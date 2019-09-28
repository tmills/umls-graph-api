package org.chboston.cnlp;

import org.apache.commons.cli.*;
import org.chboston.cnlp.graphapi.GraphFunctions;

public class CmdLine {

    public static final String MODE_FLAG = "mode";
    public static final String MODE_PARENTS = "parents";
    public static final String MODE_CHILDREN = "children";
    public static final String CUI_FLAG = "cui";

    // create Options object
    static final Options options = new Options();
    static Option modeOption = Option.builder("m").required(true).longOpt("mode").hasArg().desc("Mode to run in; options include \"parents\", \"children\" to retrieve patients or children of cui provided with -c option").build(); //, true, );
    static Option cuiOption = Option.builder("c").required(true).longOpt("cui").hasArg().desc("UMLS Concept Unique Identifier (CUI) to query").build();
    static {
        options.addOption(modeOption);
        options.addOption(cuiOption);
    }

    public static void main(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse( options, args);

        String mode = cmd.getOptionValue(MODE_FLAG);
        if(mode.equals(MODE_PARENTS)){
            System.out.println("Ancestors:");
            retrieveParents(cmd.getOptionValue(CUI_FLAG));
        }else if(mode.equals(MODE_CHILDREN)){
            System.out.println("Descendents:");
            retrieveChildren(cmd.getOptionValue(CUI_FLAG));
        }
    }

    private static void retrieveParents(String cui){
        for(String ancestorCui : GraphFunctions.getAncestors(cui)){
            System.out.println(ancestorCui);
        }
    }

    private static void retrieveChildren(String cui){
        for(String descendentCui : GraphFunctions.getDescendents(cui)){
            System.out.println((descendentCui));
        }
    }
}
