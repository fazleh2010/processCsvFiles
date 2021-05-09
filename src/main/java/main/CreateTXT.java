/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import analyzer.Lemmatizer;
import utils.FileFolderUtils;
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


/**
 *
 * @author elahi
 */
public class CreateTXT implements NullInterestingness,DirectoryLocation{

   
    public static String resultStrTxt(String outputDir, String prediction,  Lemmatizer lemmatizer,String interestingness) throws Exception {
        String stringAdd = "";
        Set<String> posTag=new HashSet<String>();
        //posTag.add("JJ");
        //posTag.add("NN");
        posTag.add("VB");
        
        for(String parts_of_speech:posTag){
             List<File> files = FileFolderUtils.getSpecificFiles(outputDir, parts_of_speech);
        if (!files.isEmpty()) {
            for (File file : files) {
              
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
