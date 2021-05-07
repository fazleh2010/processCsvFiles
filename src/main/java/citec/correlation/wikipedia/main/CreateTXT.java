/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package citec.correlation.wikipedia.main;

import citec.correlation.wikipedia.analyzer.Lemmatizer;
import citec.correlation.wikipedia.utils.FileFolderUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;
import org.javatuples.Pair;



/**
 *
 * @author elahi
 */
public class CreateTXT implements NullInterestingness,DirectoryLocation{

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
        return "No answer found";*/
    }

    public static String resultStrTxt(String outputDir, String prediction,  Lemmatizer lemmatizer,String interestingness) throws Exception {
        String stringAdd = "";
        Set<String> posTag=new HashSet<String>();
        //posTag.add("JJ");
        //posTag.add("NN");
        posTag.add("VB");
        
        for(String parts_of_speech:posTag){
             Pair<Boolean, List<File>> pair = FileFolderUtils.getSpecificFiles(outputDir, parts_of_speech);
        if (pair.getValue0()) {
            for (File file : pair.getValue1()) {
              
                Map<String, LexiconUnit> lexiconDic = getLexiconTxt(file, parts_of_speech, lemmatizer);
                for (String lexical : lexiconDic.keySet()) {
                    LexiconUnit lexiconUnit = lexiconDic.get(lexical);
                    lexical="\""+ lexical +"\"";
                    String lines = "";
                    for (Integer index : lexiconUnit.getEntityInfos().keySet()) {
                        List<String> resultList = lexiconUnit.getEntityInfos().get(index);
                        String line = "";
                        for (Integer i = 0; resultList.size() > i; i++) {
                            String value = resultList.get(i);
                            if(i==4){
                              value= value.replace("{", "");
                              value= value.replace("}", "");
                            }
                            
                            else if (value.contains("=")) {
                                 value = value.split("=")[1];
                            }
                            
                           /*if (i == 0) {
                                //String replaceStr =getAtrribute(value);
                                String key = EvaluationTriple.getType(prediction);
                                value = value.replace("kb=", key + "=");

                            }*/
                            value ="\""+ value +"\""+ ", ";
                            line += value;
                        }
                        lines += lexical + "," + line + "\n";
                    }
                    stringAdd += lines;
                }
            String csvFileName=    outputDir+file.getName().replace(".json","");
            System.out.println(file.getName());
            FileFolderUtils.writeToTextFile(stringAdd, csvFileName);
           
            }
        }
            
        }
       
         return stringAdd;
    }
    
      private static Map<String, LexiconUnit> getLexiconTxt(File file, String posTag, Lemmatizer lemmatizer) throws IOException {
        Map<String, LexiconUnit> lexiconDic = new TreeMap<String, LexiconUnit>();

        ObjectMapper mapper = new ObjectMapper();
        List<LexiconUnit> lexiconUnits = new ArrayList<LexiconUnit>();

        lexiconUnits = mapper.readValue(file, new TypeReference<List<LexiconUnit>>() {
        });
        for (LexiconUnit lexiconUnit : lexiconUnits) {
            List<LexiconUnit> modifyLexiconUnits = new ArrayList<LexiconUnit>();
            String word = lexiconUnit.getWord();

            word = lemmatizer.getGeneralizedPosTagLemma(word, posTag);
            word = word.replaceAll("\\d", " ");
            word = word.replaceAll("[^a-zA-Z0-9]", " ");
            word = word.strip().trim();
            word = word.replaceAll(" ", "_");

            if (lexiconDic.containsKey(word)) {
                LexiconUnit existLexiconUnit = lexiconDic.get(word);
                LexiconUnit newLexiconUnit = new LexiconUnit(existLexiconUnit, lexiconUnit);
                lexiconDic.put(word, newLexiconUnit);

            } else {
                lexiconDic.put(word, lexiconUnit);
            }

        }
        return lexiconDic;

    }
    
    /*private static Map<String, LexiconUnit> getLexicon(File file, String posTag, Lemmatizer lemmatizer) throws IOException {
        Map<String, LexiconUnit> lexiconDic = new TreeMap<String, LexiconUnit>();

        ObjectMapper mapper = new ObjectMapper();
        List<LexiconUnit> lexiconUnits = new ArrayList<LexiconUnit>();

        lexiconUnits = mapper.readValue(file, new TypeReference<List<LexiconUnit>>() {
        });
        for (LexiconUnit lexiconUnit : lexiconUnits) {
            List<LexiconUnit> modifyLexiconUnits = new ArrayList<LexiconUnit>();
            String word = lexiconUnit.getWord();
         
            word = lemmatizer.getGeneralizedPosTagLemma(word, posTag);
            word = word.replaceAll("\\d", " ");
            word = word.replaceAll("[^a-zA-Z0-9]", " ");
            word = word.strip().trim();
            word = word.replaceAll(" ", "_");

            if (lexiconDic.containsKey(word)) {
                LexiconUnit existLexiconUnit = lexiconDic.get(word);
                LexiconUnit newLexiconUnit = new LexiconUnit(existLexiconUnit, lexiconUnit);
                lexiconDic.put(word, newLexiconUnit);

            } else {
                lexiconDic.put(word, lexiconUnit);
            }

        }
        return lexiconDic;

    }*/

 

}
