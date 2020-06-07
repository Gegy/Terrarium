package net.gegy1000.earth.server.util.debug;

import net.gegy1000.earth.server.world.ecology.soil.SoilSuborder;

public final class SoilColors {
    public static int get(SoilSuborder soil) {
        switch (soil) {
            case OCEAN: return 0x0000FF;
            case SHIFTING_SAND: return 0xFFF27F;
            case ROCK: return 0xA0A0A0;
            case ICE: return 0x92FAFF;
            case HISTELS: return 0x48D1CC;
            case TURBELS: return 0x43D4D2;
            case ORTHELS: return 0x4EC8CC;
            case FOLISTS: return 0xA52A2A;
            case FIBRISTS: return 0xB22328;
            case HEMISTS: return 0xB41919;
            case SAPRISTS: return 0xA42828;
            case AQUODS: return 0xD8BFD8;
            case CRYODS: return 0xD4C3D4;
            case HUMODS: return 0xD2BAD2;
            case ORTHODS: return 0xD5C0D5;
            case GELODS: return 0xDDB9DD;
            case AQUANDS: return 0xFF00FF;
            case CRYANDS: return 0xFA02FA;
            case TORRANDS: return 0xFC05FA;
            case XERANDS: return 0xFF0AFF;
            case VITRANDS: return 0xFC04F5;
            case USTANDS: return 0xF50CF0;
            case UDANDS: return 0xF100F1;
            case GELANDS: return 0xEB05EB;
            case AQUOX: return 0xFF0000;
            case TORROX: return 0xF50505;
            case USTOX: return 0xF20A0A;
            case PEROX: return 0xFB0202;
            case UDOX: return 0xFF0E0E;
            case AQUERTS: return 0xFFFF00;
            case CRYERTS: return 0xF1F100;
            case XERERTS: return 0xFAFA05;
            case TORRERTS: return 0xEBEB0C;
            case USTERTS: return 0xF5EB00;
            case UDERTS: return 0xEEFF06;
            case CRYIDS: return 0xFFDAB9;
            case SALIDS: return 0xF5D7BB;
            case DURIDS: return 0xF5D3B9;
            case GYPSIDS: return 0xE8C8B8;
            case ARGIDS: return 0xFFDDC2;
            case CALCIDS: return 0xE7CDC0;
            case CAMBIDS: return 0xF3E3C8;
            case AQUULTS: return 0xFFA500;
            case HUMULTS: return 0xF3A702;
            case UDULTS: return 0xFB9C00;
            case USTULTS: return 0xF0B005;
            case XERULTS: return 0xF7980F;
            case BOROLLS: return 0x09FE03;
            case ALBOLLS: return 0x00FF00;
            case AQUOLLS: return 0x03FF05;
            case RENDOLLS: return 0x05F300;
            case XEROLLS: return 0x02F00A;
            case CRYOLLS: return 0x0FEA03;
            case USTOLLS: return 0x00F000;
            case UDOLLS: return 0x0CFF0C;
            case GELOLLS: return 0x14DD14;
            case AQUALFS: return 0xADFF2F;
            case CRYALFS: return 0xA5FF2F;
            case USTALFS: return 0x8CFF37;
            case XERALFS: return 0xAFFF19;
            case UDALFS: return 0x8CFF19;
            case UDEPTS: return 0xCD5C5C;
            case GELEPTS: return 0xCB5A5F;
            case OCHREPTS: return 0xCA5960;
            case AQUEPTS: return 0xCF595C;
            case ANTHREPTS: return 0xD64B55;
            case CRYEPTS: return 0xE05C5D;
            case USTEPTS: return 0xD35740;
            case XEREPTS: return 0xD95F35;
            case AQUENTS: return 0x7FFFD4;
            case ARENTS: return 0x7DFFD2;
            case PSAMMENTS: return 0x86F5CD;
            case FLUVENTS: return 0x73FFD2;
            case ORTHENTS: return 0x88EEC8;
            default:
            case NO: return 0;
        }
    }
}
