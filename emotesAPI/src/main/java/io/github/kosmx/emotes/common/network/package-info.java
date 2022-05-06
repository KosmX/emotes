package io.github.kosmx.emotes.common.network;

/*
IDS

0 - emoteData v0
1 - PlayerData v0
8 - configExchange (Discovery) v8- its version is the common network version
10 - stopData v0
0x11 - EmoteHeader
0x12 - EmoteIcon

3 - song -> ver0: no sound, ver1 current

ID > 0x80 - config bits

0x80 does server track play states [bool]

 */