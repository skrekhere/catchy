package dev.shrek.catchy.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Arrays;
import java.util.List;

public class ModConf {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final General GENERAL = new General(BUILDER);
    public static final ForgeConfigSpec spec = BUILDER.build();

    public static List<String> BLACKLISTED;

    public static void load(){
        BLACKLISTED = GENERAL.blacklist.get();
    }

    public static class General {
        public final ForgeConfigSpec.ConfigValue<List<String>> blacklist;

        public General(ForgeConfigSpec.Builder builder){
            String[] defaultList = new String[]{"minecraft:player"};
            builder.push("General");
            blacklist = builder
                    .comment("Blacklist any mobs from being captured by Big Jar.")
                    .define("blacklist", Arrays.asList(defaultList));
        }
    }
}
