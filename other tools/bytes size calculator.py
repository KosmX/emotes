# This script is for calculating size of known data structures

if __name__ == '__main__':
    inp = input().upper()
    sizeSum = 0
    for i in inp:
        if i == 'B':  # B as byte or byte sized boolean
            sizeSum += 1
        elif i == 'S' or i == 'C':  # S as short C as char
            sizeSum += 2
        elif i == 'I' or i == 'F':  # I as int F as float
            sizeSum += 4
        elif i == 'L' or i == 'D':  # L as long int D as double
            sizeSum += 8
        else:
            print("I don't know what to do with \"{}\".".format(i))
    print(sizeSum)
