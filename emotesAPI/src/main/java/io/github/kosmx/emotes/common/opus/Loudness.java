package io.github.kosmx.emotes.common.opus;

import java.util.Arrays;

/**
 * Integrated loudness by ITU-R BS.1770 with EBU R128 gating, fed as the audio is decoded.
 */
public final class Loudness {
    // Blocks are 400 ms starting every 100 ms; banking the quarters filters the signal once
    private static final int STEP_MS = 100;
    private static final int STEPS_PER_BLOCK = 4;

    private static final double ABSOLUTE_GATE = -70.0;
    private static final double RELATIVE_GATE = -10.0;
    private static final double OFFSET = -0.691;

    private final int step;
    private final KWeighting filter = new KWeighting();

    private double[] steps;
    private int stepCount;

    private double stepSum;
    private int stepFilled;

    public Loudness(int expectedSamples) {
        this.step = OpusPackets.SAMPLE_RATE * STEP_MS / 1000;
        this.steps = new double[Math.max(1, expectedSamples / this.step + 1)];
    }

    public void feed(short[] samples, int offset, int length) {
        for (int i = offset, end = offset + length; i < end; i++) {
            double weighted = this.filter.apply(samples[i] / 32768.0);
            this.stepSum += weighted * weighted;

            if (++this.stepFilled == this.step) {
                if (this.stepCount == this.steps.length) {
                    this.steps = Arrays.copyOf(this.steps, this.steps.length * 2);
                }
                this.steps[this.stepCount++] = this.stepSum;
                this.stepSum = 0;
                this.stepFilled = 0;
            }
        }
    }

    /**
     * @return LUFS, or negative infinity when nothing rises above the absolute gate
     */
    public double integrated() {
        if (this.stepCount < STEPS_PER_BLOCK) return Double.NEGATIVE_INFINITY;

        double[] blocks = new double[this.stepCount - STEPS_PER_BLOCK + 1];
        int block = this.step * STEPS_PER_BLOCK;
        for (int i = 0; i < blocks.length; i++) {
            double sum = 0;
            for (int j = 0; j < STEPS_PER_BLOCK; j++) sum += this.steps[i + j];
            blocks[i] = sum / block;
        }

        double loud = gatedMean(blocks, ABSOLUTE_GATE);
        if (Double.isNaN(loud)) return Double.NEGATIVE_INFINITY;

        double mean = gatedMean(blocks, loudness(loud) + RELATIVE_GATE);
        return Double.isNaN(mean) ? Double.NEGATIVE_INFINITY : loudness(mean);
    }

    private static double gatedMean(double[] blocks, double gate) {
        double sum = 0;
        int count = 0;
        for (double energy : blocks) {
            if (loudness(energy) > gate) {
                sum += energy;
                count++;
            }
        }
        return count == 0 ? Double.NaN : sum / count;
    }

    private static double loudness(double energy) {
        return energy <= 0 ? Double.NEGATIVE_INFINITY : OFFSET + 10 * Math.log10(energy);
    }

    /**
     * High shelf then high pass, coefficients as BS.1770 tabulates them for 48 kHz.
     */
    private static final class KWeighting {
        private static final double SHELF_B0 = 1.53512485958697;
        private static final double SHELF_B1 = -2.69169618940638;
        private static final double SHELF_B2 = 1.19839281085285;
        private static final double SHELF_A1 = -1.69065929318241;
        private static final double SHELF_A2 = 0.73248077421585;

        private static final double PASS_B0 = 1.0;
        private static final double PASS_B1 = -2.0;
        private static final double PASS_B2 = 1.0;
        private static final double PASS_A1 = -1.99004745483398;
        private static final double PASS_A2 = 0.99007225036621;

        private double shelfX1, shelfX2, shelfY1, shelfY2;
        private double passX1, passX2, passY1, passY2;

        double apply(double sample) {
            double shelf = SHELF_B0 * sample + SHELF_B1 * this.shelfX1 + SHELF_B2 * this.shelfX2
                    - SHELF_A1 * this.shelfY1 - SHELF_A2 * this.shelfY2;
            this.shelfX2 = this.shelfX1;
            this.shelfX1 = sample;
            this.shelfY2 = this.shelfY1;
            this.shelfY1 = shelf;

            double pass = PASS_B0 * shelf + PASS_B1 * this.passX1 + PASS_B2 * this.passX2
                    - PASS_A1 * this.passY1 - PASS_A2 * this.passY2;
            this.passX2 = this.passX1;
            this.passX1 = shelf;
            this.passY2 = this.passY1;
            this.passY1 = pass;

            return pass;
        }
    }
}
