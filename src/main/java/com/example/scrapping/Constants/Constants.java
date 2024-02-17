package com.example.scrapping.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Constants {
    public static final String MOTORCYCLESPECS_CO_ZA = "https://www.motorcyclespecs.co.za/";
    public static final String INSERT_MOTO_SCRIPT = "Statements/insert_moto.txt";
    public static final String COUNT_MOTO_BY_URL = "Statements/count_moto_by_url.txt";
    public static final String ERRORS_LOG_TXT_FILE = "src/main/resources/errors_log.txt";
    public static final String NOT_INSERTED_MODELS_JSON_LOG_FILE = "src/main/resources/not_inserted_models_log.json";
    public static final String EXCEPTION_NO_FIELDS = "NO_FIELDS";
    public static final String EXCEPTION_TABLE_24_MISSING = "TABLE_24_MISSING";

    //      Models with missing specs to ignore
    public static final List<String> MISSING_TORQUE_LIST = new ArrayList<>(Arrays.asList(
            "https://www.motorcyclespecs.co.za/model/husqvana/Husqvarna_FE501%2016.htm",
            "https://www.motorcyclespecs.co.za/model/husqvana/Husqvarna_FE350S%2015.htm",
            "https://www.motorcyclespecs.co.za/model/husqvana/husqvarna_fe501_18.html",
            "https://www.motorcyclespecs.co.za/model/husqvana/Husqvarna_FE350-14.htm",
            "https://www.motorcyclespecs.co.za/model/husqvana/Husqvarna_FE501S.htm",
            "https://www.motorcyclespecs.co.za/model/husqvana/Husqvarna_FE501%2014.htm",
            "https://www.motorcyclespecs.co.za/model/triu/triumph-tiger-900-gt-2024.html",
            "https://www.motorcyclespecs.co.za/model/husqvana/Husqvarna_FE250-16.htm",
            "https://www.motorcyclespecs.co.za/model/husqvana/Husqvarna_FE350-16.htm",
            "https://www.motorcyclespecs.co.za/model/Custom/campagna_trex_16SP.htm",
            "https://www.motorcyclespecs.co.za/model/husqvana/husqvarna_fe501_20.html",
            "https://www.motorcyclespecs.co.za/model/kawasaki/kawasaki-ninja7-hybrid-2024.html",
            "https://www.motorcyclespecs.co.za/model/kawasaki/kawasaki-z7-hybrid-2023.html",
            "https://www.motorcyclespecs.co.za/model/husqvana/husqvarna_fe501_22.html"
    ));
    public static final List<String> MISSING_POWER_LIST = new ArrayList<>(Arrays.asList(
            "https://www.motorcyclespecs.co.za/model/husqvana/Husqvarna_FE250-14.htm",
            "https://www.motorcyclespecs.co.za/model/kawasaki/kawasaki-eliminator-450-2023.html",
            "https://www.motorcyclespecs.co.za/model/kawasaki/kawasaki-z7-hybrid-2023.html",
            "https://www.motorcyclespecs.co.za/model/kawasaki/kawasaki-zx-4rr-ninja-krt-2023.html",
            "https://www.motorcyclespecs.co.za/model/kawasaki/kawasaki-ninja7-hybrid-2024.html",
            "https://www.motorcyclespecs.co.za/model/SYM/SYM%20e-Virid.htm",
            "https://www.motorcyclespecs.co.za/model/husqvana/Husqvarna_FE250-16.htm"
    ));
    public static final List<String> MISSING_WEIGHT_LIST = new ArrayList<>(Arrays.asList(
            "https://www.motorcyclespecs.co.za/model/kawasaki/kawasaki-z7-hybrid-2023.html",
            "https://www.motorcyclespecs.co.za/model/Piaggio/piaggio_mp3_400_23.html",
            "https://www.motorcyclespecs.co.za/model/Piaggio/piaggio_mp3-300_hpe_20.html",
            "https://www.motorcyclespecs.co.za/model/suzu/suzuki_V-Strom_1050xt_20.html",
            "https://www.motorcyclespecs.co.za/model/Victory/victory_gunner%2015.htm",
            "https://www.motorcyclespecs.co.za/model/triu/Triumph_Bonneville_Bobber_tfc.html",
            "https://www.motorcyclespecs.co.za/model/suzu/suzuki_V-Strom_1050xt_adven_20.html",
            "https://www.motorcyclespecs.co.za/model/Piaggio/piaggio_mp3_530_exclusive_23.html"
    ));

    public static final List<String> MISSING_RESERVE_LIST = new ArrayList<>(Arrays.asList(
            "https://www.motorcyclespecs.co.za/model/Moto%20Morini/moto-morini-milano-1200-2024-.html"
    ));
    public static final List<String> MISSING_SPEED_LIST = new ArrayList<>(Arrays.asList(
            "https://www.motorcyclespecs.co.za/model/triu/triumph_speedmaster%2013.htm",
            "https://www.motorcyclespecs.co.za/model/velocette/velocette_GTP.htm",
            "https://www.motorcyclespecs.co.za/model/velocette/Velocette_KSS_KTT.html"
    ));

    public static final List<String> MISSING_SEAT_LIST = new ArrayList<>(Arrays.asList(
            "https://www.motorcyclespecs.co.za/model/kawasaki/Kawasaki_zx25r_ninja_22.html",
            "https://www.motorcyclespecs.co.za/model/kawasaki/Kawasaki_zx25r_ninja.html",
            "https://www.motorcyclespecs.co.za/model/triu/Triumph_Bonneville_Bobber_tfc.html",
            "https://www.motorcyclespecs.co.za/model/XTR_Radical/Ducati_Monster_Pata_Negra.htm",
            "https://www.motorcyclespecs.co.za/model/kawasaki/kawasaki-z7-hybrid-2023.html"
    ));

    public static final List<String> MODEL_LINKS_TO_INGNORE_LIST = new ArrayList<>(Arrays.asList(
            "https://www.motorcyclespecs.co.za/model/aprilia/aprilia_sx_125%2009.htm",
            "https://www.motorcyclespecs.co.za/model/Custom/yamaha_sr_500.htm",
            "https://www.motorcyclespecs.co.za/Ducati2.html",
            "https://www.motorcyclespecs.co.za/Hero.html",
            "https://www.motorcyclespecs.co.za//en.wikipedia.org",
            "https://www.motorcyclespecs.co.za/Honda2.html",
            "https://www.motorcyclespecs.co.za/Husquavana2.html",
            "https://www.motorcyclespecs.co.za/Husquavana3.html",
            "https://www.motorcyclespecs.co.za/Indian2.html",
            "https://www.motorcyclespecs.co.za/Kawasaki.htm",
            "https://www.motorcyclespecs.co.za/Kawasaki2.html",
            "https://www.motorcyclespecs.co.za/model/Honda/honda_rc51_sp2_nicky_hayden.html",
            "https://www.motorcyclespecs.co.za/Moto_Guzzi2.html",
            "https://www.motorcyclespecs.co.za/mv_agusta.html",
            "https://www.motorcyclespecs.co.za/model/RSD/tracker.htm",
            "https://www.motorcyclespecs.co.za/suzuki3.html",
            "https://www.motorcyclespecs.co.za/H-D.htm",
            "https://www.motorcyclespecs.co.za/suzuki2.html",
            "https://www.motorcyclespecs.co.za/model/Confederate/Confederate_FA-13_Combat_Bomber.htm",
            "https://www.motorcyclespecs.co.za/model/Confederate/confederate_fighter.htm",
            "https://www.motorcyclespecs.co.za/model/Confederate/Curtiss-Warhawk.html",
            "https://www.motorcyclespecs.co.za/model/Confederate/confederate_f131_hellcat.htm",
            "https://www.motorcyclespecs.co.za/model/Custom/wrenchmonkees_kawasaki_z1000.htm"
    ));

}
