/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author elahi
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import analyzer.Lemmatizer;
import utils.CsvFile;
import utils.FileFolderUtils;
import utils.PropertyCSV;
import utils.StopWordRemoval;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.CreateTXT;
import main.Lexicon;
import main.LineInfo;
import main.NullInterestingness;
import static main.NullInterestingness.AllConf;
import static main.NullInterestingness.OBJECT;
import static main.NullInterestingness.PREDICATE;
import main.PredictionRules;

/**
 *
 * @author elahi //BusinessPerson malformed CSV o
 */
public class Main implements NullInterestingness,PredictionRules {

    public static String createLexicon = "createLexicon";
    static String createExperimentLine = "createExperimentLine";

    public Main(String rawFileDir, String outputDir, String givenPrediction, String givenInterestingness, Logger givenLOGGER, String fileType, String creationType) throws Exception {
        // this.lemmatizer=lemmatizer;

        for (String prediction : PredictionRules.predictKBGivenLInguistic) {
           
            if (!prediction.equals(givenPrediction)) {
                continue;
            }
            for (String rule : interestingness) {
                List<File> files = new  ArrayList<File>();
                if (givenInterestingness != null) {
                    if (!rule.contains(givenInterestingness)) {
                        continue;
                    }
                }
                if (creationType.contains(createExperimentLine)) {
                    
                    //files = FileFolderUtils.getSpecificFiles(rawFileDir, ".csv");
                      files = FileFolderUtils.getSpecificFiles(rawFileDir, givenPrediction+"-", ".csv");
                    if (!files.isEmpty()) {
                        createExperimentLinesCsv(outputDir, prediction, rule, files, creationType);
                    } else {
                        throw new Exception("NO files found for " + prediction + " " + rawFileDir);
                    }

                }
            }
        }
    }

    private static void createExperimentLinesCsv(String directory, String dbo_prediction, String interestingness, List<File> classFiles, String creationType) throws Exception {

        List<String[]> rows = new ArrayList<String[]>();
        Integer numberOfClass = 0;
        Integer maximumNumberOflines = 300000;

        for (File classFile : classFiles) {
            Map<String, List<LineInfo>> lineLexicon = new TreeMap<String, List<LineInfo>>();
            String fileName = classFile.getName();
            /*if (!fileName.contains("AcademicJournal")) {
                continue;
            }*/
            CsvFile csvFile = new CsvFile(classFile);
            //rows = csvFile.getManualRow(classFile, 1000.0, 300000);
            rows = csvFile.getRows(classFile);
            PropertyCSV propertyCSV = null;
            numberOfClass = numberOfClass + 1;
            String className = classFile.getName().replace("http%3A%2F%2Fdbpedia.org%2Fontology%2F", "");
            System.out.println("interestingness:"+interestingness+" now running clssName::"+className+" "+dbo_prediction);
            if (classFile.getName().contains(PropertyCSV.localized)) {
                propertyCSV = new PropertyCSV(PropertyCSV.localized);
            } else {
                propertyCSV = new PropertyCSV(PropertyCSV.general);
            }
            Integer index = 0;
            for (String[] row : rows) {
                 
                LineInfo lineInfo = new LineInfo(index, row, dbo_prediction, interestingness, propertyCSV);
              
                if (lineInfo.getLine() != null) {
                    if (lineInfo.getLine().contains("XMLSchema#integer")||lineInfo.getLine().contains("XMLSchema#gYear")) {
                        continue;
                    }
                    else if(lineInfo.getProbabilityValue().isEmpty())
                         continue;

                }
               /*for(String string:row){
                     if(string.contains("real")){
                      //System.out.println(string);
  
                     }
             
  
                 }*/

                try {
                    String nGram = lineInfo.getWord();
                    nGram = nGram.replace("\"", "");
                    nGram = nGram.toLowerCase().trim().strip();
                    nGram = nGram.replaceAll(" ", "_");
                    nGram = StopWordRemoval.deleteStopWord(nGram);

                    List<LineInfo> results = new ArrayList<LineInfo>();
                    if (lineLexicon.containsKey(nGram)) {
                        results = lineLexicon.get(nGram);
                        results.add(lineInfo);
                        lineLexicon.put(nGram, results);
                    } else {
                        results.add(lineInfo);
                        lineLexicon.put(nGram, results);

                    }
                } catch (Exception ex) {
                   // System.out.println("nGram::"+nGram);
                    continue;
                }
               
            }
            Lexicon lexicon = new Lexicon(directory);
            lexicon.preparePropertyLexicon(dbo_prediction, directory, className, interestingness, lineLexicon);

        }
        

    }

    private static Lexicon createLexicon(String qald9Dir,String directory, String dbo_prediction, String interestingness, String experimentID, Integer numberOfRules) throws Exception {
        Map<String, List<LineInfo>> lineLexicon = new TreeMap<String, List<LineInfo>>();
        List<String[]> rows = new ArrayList<String[]>();
        PropertyCSV propertyCSV = null;
        if (dbo_prediction.contains(PropertyCSV.localized)) {
            propertyCSV = new PropertyCSV(PropertyCSV.localized);
        } else {
            propertyCSV = new PropertyCSV(PropertyCSV.general);
        }

        File file = new File(directory + "/" + experimentID);
        CsvFile csvFile = new CsvFile(file);
        rows = csvFile.getRows(file);
        //rows = csvFile.getRows(file, 1000.0, 300000);

        Integer index = 0, rowCount = 0;
        for (String[] row : rows) {
            if (rowCount == 0) {
                rowCount = rowCount + 1;
                continue;
            } else {
                rowCount = rowCount + 1;
            }
            LineInfo lineInfo = new LineInfo(index, row, dbo_prediction, interestingness, propertyCSV);
            index = index + 1;

            if (index >= numberOfRules) {
                break;
            }
            if (!lineInfo.getValidFlag()) {
                continue;
            }

            String nGram = lineInfo.getWord();
            nGram = nGram.replace("\"", "");
            nGram = nGram.toLowerCase().trim().strip();
            nGram = nGram.replaceAll(" ", "_");
            nGram = StopWordRemoval.deleteStopWord(nGram);

            List<LineInfo> results = new ArrayList<LineInfo>();
            if (lineLexicon.containsKey(nGram)) {
                results = lineLexicon.get(nGram);
                results.add(lineInfo);
                lineLexicon.put(nGram, results);
            } else {
                results.add(lineInfo);
                lineLexicon.put(nGram, results);

            }

        }
        Lexicon lexicon = new Lexicon(qald9Dir);
        lexicon.preparePropertyLexicon(dbo_prediction, directory, experimentID, interestingness, lineLexicon);
        return lexicon;
    }

    private static Lexicon createExperimentGrep(String qald9Dir,String directory, String dbo_prediction, String interestingness, String experimentID, Integer numberOfRules) throws Exception {
        Map<String, List<LineInfo>> lineLexicon = new TreeMap<String, List<LineInfo>>();
        List<String[]> rows = new ArrayList<String[]>();
        PropertyCSV propertyCSV = null;
        if (dbo_prediction.contains(PropertyCSV.localized)) {
            propertyCSV = new PropertyCSV(PropertyCSV.localized);
        } else {
            propertyCSV = new PropertyCSV(PropertyCSV.general);
        }

        File file = new File(directory + "/" + experimentID);
        CsvFile csvFile = new CsvFile(file);
        rows = csvFile.getRows(file);

        Integer index = 0, rowCount = 0;
        for (String[] row : rows) {
            if (rowCount == 0) {
                rowCount = rowCount + 1;
                continue;
            } else {
                rowCount = rowCount + 1;
            }
            LineInfo lineInfo = new LineInfo(index, row, dbo_prediction, interestingness, propertyCSV);
            index = index + 1;

            if (index >= numberOfRules) {
                break;
            }
            if (!lineInfo.getValidFlag()) {
                continue;
            }

            String nGram = lineInfo.getWord();
            nGram = nGram.replace("\"", "");
            nGram = nGram.toLowerCase().trim().strip();
            nGram = nGram.replaceAll(" ", "_");
            nGram = StopWordRemoval.deleteStopWord(nGram);

            List<LineInfo> results = new ArrayList<LineInfo>();
            if (lineLexicon.containsKey(nGram)) {
                results = lineLexicon.get(nGram);
                results.add(lineInfo);
                lineLexicon.put(nGram, results);
            } else {
                results.add(lineInfo);
                lineLexicon.put(nGram, results);

            }

        }
        Lexicon lexicon = new Lexicon(qald9Dir);
        lexicon.preparePropertyLexicon(dbo_prediction, directory, experimentID, interestingness, lineLexicon);
        return lexicon;
    }

    private static String[] findParameter(String[] info) {
        String[] parameters = new String[3];
        for (Integer index = 0; index < info.length; index++) {
            if (index == 0) {
                parameters[index] = info[index];
            }
            if (index == 1) {
                parameters[index] = info[index];
            } else if (index == 2) {
                parameters[index] = info[index];
            }
        }
        return parameters;
    }

  

    public static boolean isKBValid(String word) {

        if (word.contains("#integer") || word.contains("#double")) {
            return true;
        }
        return false;
    }

    public static void main(String[] args) throws Exception {
        //create experiments
        createExperiments();

    }

    public static void createExperiments() throws Exception {
        //String qald9Dir = "src/main/resources/data/";
        String baseDir = "/opt/rulepatterns/results-v5/";
        String resourceDir = "/var/www/html/ontologyLexicalization/resources/data/";
        //String baseDir = "/home/elahi/new/dbpediaFiles/unlimited/unlimited/";
        //String resourceDir = "src/main/resources/";
        
        Set<String> posTag=new HashSet<String>();
       // posTag.add("JJ");
       // posTag.add("NN");
        posTag.add("VB");
       

        Logger LOGGER = Logger.getLogger(CreateTXT.class.getName());
        String outputDir = resourceDir;
        String creationType = createExperimentLine;
        Lemmatizer lemmatizer = new Lemmatizer();
        //String  txtDir =  "src/main/resources/data" + "/" + "txt" +  "/"  ;
        //String  txtDir =  "src/main/resources/data" + "/" + "txt" +  "/"  ;
        String  txtDir =  resourceDir + "/" + "txt" +  "/"  ;
        //txtDir = "/var/www/html/ontologyLexicalization/data/";

        FileFolderUtils.createDirectory(txtDir);
        
       /* 

       /* List<String> predictLinguisticGivenKB = new ArrayList<String>(Arrays.asList(
                //predict_sp_for_o_given_localized_l
                //predict_l_for_s_given_po
                //predict_l_for_s_given_o
                //predict_l_for_o_given_p,
                //predict_l_for_o_given_s,
                //predict_l_for_s_given_p,
                //predict_localized_l_for_o_given_p
                //predict_l_for_o_given_sp
                //predict_localized_l_for_s_given_p 
                //predict_po_for_s_given_l
                //PredictionRules.predict_po_for_s_given_localized_l,
                //PredictionRules.predict_p_for_s_given_localized_l
               // PredictionRules.predict_p_for_o_given_localized_l
        ));*/
       
        List<String> predictKBGivenLInguistic = new ArrayList<String>(Arrays.asList(
                predict_p_for_o_given_localized_l ,
                predict_p_for_s_given_localized_l,
                /*predict_po_for_s_given_l,
                predict_p_for_o_given_l,
                predict_p_for_s_given_l,
                predict_o_for_s_given_l,
                predict_s_for_o_given_l,
                predict_sp_for_o_given_localized_l,
                predict_sp_for_o_given_l*/
        ));

        
      
        
        List<String> interestingness = new ArrayList<String>();
        interestingness.add(Coherence);
        interestingness.add(Cosine);
        interestingness.add(AllConf);
        interestingness.add(Kulczynski);
        interestingness.add(IR);
        interestingness.add(MaxConf);
        for (String prediction : predictKBGivenLInguistic) {
            //String inputDir = baseDir + prediction + "/" ;
             String inputDir = baseDir + "/" ;
           
            for (String inter : interestingness) {
                outputDir = resourceDir + "/" + prediction + "/" + inter+"/";
                FileFolderUtils.createDirectory(outputDir);
                //Main ProcessFile = new Main(inputDir, outputDir, prediction, inter, LOGGER, ".csv", creationType);
                //System.out.println(outputDir);
                CreateTXT.resultStrTxt(posTag,outputDir,txtDir, prediction, lemmatizer, inter);
            }
        }
        
        

    }
    
    /*
     public static void main(String str[]) throws Exception {
        Logger LOGGER = Logger.getLogger(CreateTXT.class.getName());
        Lemmatizer lemmatizer = new Lemmatizer();
        
         List<String> predictLinguisticGivenKB = new ArrayList<String>(Arrays.asList(//predict_l_for_o_given_p
                //predict_l_for_s_given_po
                //predict_l_for_s_given_o
                //predict_l_for_o_given_p,
                //predict_l_for_o_given_s,
                //predict_l_for_s_given_p,
                //predict_localized_l_for_o_given_p
                //predict_l_for_o_given_sp
                //predict_localized_l_for_s_given_p 
                //predict_po_for_s_given_l
                //PredictionRules.predict_po_for_s_given_localized_l
        PredictionRules.predict_p_for_s_given_localized_l
        //PredictionRules.predict_p_for_o_given_localized_l
        ));

        String prediction = PredictionRules.predict_p_for_s_given_localized_l, parts_of_speech = "JJ";
        String outputDir = qald9Dir + "/" + prediction + "/" + "dic/";
        String stringAdd = "";
        Boolean flag = false;
        //String experimentID=prediction+"_"+className+".csv";
        
        List<String> interestingness = new ArrayList<String>();
        //interestingness.add(Cosine);
        //interestingness.add(Coherence);
        interestingness.add(AllConf);
        //interestingness.add(Kulczynski);
         //interestingness.add(IR);
        //interestingness.add(MaxConf);
        
               
        for (String inter : interestingness) {
            stringAdd = resultStrTxt(outputDir, prediction, lemmatizer, inter);
            //FileFolderUtils.writeToTextFile(stringAdd, outputDir+experimentID);
            System.out.println(stringAdd);

        }

        /*prediction = str[0];
        className = str[1];
        lexicalElement = str[2];
        parts_of_speech = str[3];
        if (str.length < 4) {
            throw new Exception("less number of argument!!!");
        }
         */
 /* Pair<Boolean, List<File>> pair = FileFolderUtils.getSpecificFiles(outputDir, className, parts_of_speech);
        if (pair.getValue0()) {
            for (File file : pair.getValue1()) {
                Map<String, LexiconUnit> lexiconDic = getLexicon(file, lexicalElement, lemmatizer);
                if (lexiconDic.containsKey(lexicalElement)) {
                    flag=true;
                    stringAdd = "lexical entry=" + lexicalElement + " ,class=" + className + " ,rule=" + prediction + "\n";
                    LexiconUnit lexiconUnit = lexiconDic.get(lexicalElement);
                    String lines = "";
                    for (Integer index : lexiconUnit.getEntityInfos().keySet()) {
                        List<String> resultList = lexiconUnit.getEntityInfos().get(index);
                        String line = "";
                        for (Integer i = 0; resultList.size() > i; i++) {
                            String value = resultList.get(i);
                            if (i == 0) {
                                //String replaceStr =getAtrribute(value);
                                String key = EvaluationTriple.getType(prediction);
                                value=value.replace("kb=", key);
                                
                            }
                            value = value + ", ";
                            line += value;
                        }
                        lines += line + "\n";
                    }
                    stringAdd += lines;
                    
                }

            }
        }
        if(flag)
        return "No answer found";
    }

     */

    @Override
    public Boolean isPredict_l_for_s_given_po(String predictionRule) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Boolean isPredict_l_for_s_given_o(String predictionRule) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Boolean isPredict_l_for_o_given_s(String predictionRule) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Boolean isPredict_l_for_o_given_sp(String predictionRule) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Boolean isPredict_l_for_o_given_p(String predictionRule) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Boolean isPredict_l_for_s_given_p(String predictionRule) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Boolean isPredict_localized_l_for_s_given_p(String predictionRule) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Boolean isPredict_po_for_s_given_l(String predictionRule) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Boolean isPredict_po_for_s_given_localized_l(String predictionRule) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Boolean isPredict_p_for_s_given_localized_l(String predictionRule) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Boolean isPredict_p_for_o_given_localized_l(String predictionRule) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
/*
<option value="2">c_s,ll_s => po (predict_po_for_s_given_localized_l) </option>
<option value="7">c_o,ll_o=>sp (predict_sp_for_o_given_localized_l)</option>
<option value="10">c_o,ll_o=>p (predict_p_for_o_given_localized_l)</option>
<option value="4">c_s,ll_s=> p (predict_p_for_s_given_localized_l)</option>
<option value="1">c_s,l_s => po (predict_po_for_s_given_l)</option>
<option value="3">c_s,l_s => p (predict_p_for_s_given_l)</option>
<option value="5">c_s,l_s => o (predict_o_for_s_given_l)</option>
<option value="6">c_o,l_o=>sp (predict_sp_for_o_given_l)</option>
<option value="8">c_o,l_o=>s (predict_s_for_o_given_l)</option>
<option value="9">c_o,l_o=>p (predict_p_for_o_given_l)</option>
    
    
<option value="11">c_s,po=>l_s (predict_l_for_s_given_po)</option>
<option value="12">c_s,po=>ll_s (predict_localized_l_for_s_given_po)</option>
<option value="13">c_s,p=>l_s (predict_l_for_s_given_p)</option>
<option value="14">c_s,p=>ll_s (predict_localized_l_for_s_given_p)</option>
<option value="15">c_s,o=>l_s (predict_l_for_s_given_o)</option>
<option value="16">c_o,sp=>l_o (predict_l_for_o_given_sp)</option>
<option value="17">c_o,sp=>ll_o (predict_localized_l_for_o_given_sp)</option>
<option value="18">c_o,s=>l_o (predict_l_for_o_given_s)</option>
<option value="19">c_o,p=>l_o (predict_l_for_o_given_p)</option>
<option value="20">c_o,p=>ll_o (predict_localized_l_for_o_given_p)</option>
    */

}

