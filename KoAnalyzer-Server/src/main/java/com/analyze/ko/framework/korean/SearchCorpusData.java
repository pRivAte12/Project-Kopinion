package com.analyze.ko.framework.korean;

import com.analyze.ko.framework.korean.DataClass.SentimentTypeInterface;
import com.opencsv.CSVReader;

import java.io.FileReader;
import java.io.IOException;

/**
 * Created by parkjaesung on 2016. 6. 29..
 */
public class SearchCorpusData implements SentimentTypeInterface {

    //TODO Reset filePath
    //set filePath in property file
    private static String filePath = System.getProperty("user.dir") + "/KoAnalyzer-Server/src/main/resources/SentimentData/corpus.csv";

    //private CSVReader reader;
    private static SearchCorpusData instance =  new SearchCorpusData();


    //constructor
    private SearchCorpusData(){

    }

    public static SearchCorpusData getInstance(){
        return instance;
    }

    // if tag type == Seed => get sentiment from index 6
    // if tag type == SubjTag => get sentiment from index 7
    public SentimentType getSentimentFromCol(String[] nextLine, CharSequence word, int index) {

        SentimentType result = SentimentType.NODATA;

        if (nextLine[3].contains(word)) {

            //  isWordDataExists = true;
            switch (nextLine[index]) {
                case "POS":
                    result = SentimentType.POS;
                    break;
                case "NEG":
                    result = SentimentType.NEG;
                    break;
                case "NEUT":
                    result = SentimentType.NEUT;
                    break;
                default:
                    result = SentimentType.NODATA;

            }
        }

        return result;
    }

    //true : if word has positive sentiment
    //false : if word has negative sentiment
    //리팩토링 필요 : 긍정이 아니면 무조건 false이므로, 중립적인단어임에도 불구하고 부정으로 판별될 수 있음
    public SentimentType getWordSentimentType(CharSequence word)throws IOException{
        SentimentType result = SentimentType.NODATA;

        int posCount = 0;
        int negCount = 0;

        CSVReader reader = new CSVReader(new FileReader(filePath));

        String[] nextLine;

        SentimentType sentimentType;
        while ((nextLine = reader.readNext()) != null) {
            // nextLine[] is an array of values from the line

            switch (nextLine[2]){
                case "Seed" :
                    sentimentType = getSentimentFromCol(nextLine, word, 6);

                    if(sentimentType.equals(SentimentType.POS)){
                        posCount++;
                    }else if(sentimentType.equals(SentimentType.NEG)){
                        negCount++;
                    }
                    break;

            }
        }

        if(posCount > negCount){
            result = SentimentType.POS;
        }else if(posCount < negCount){
            result = SentimentType.NEG;
        }else if(posCount == 0 && negCount == 0){
            result = SentimentType.NEUT;
        }
        else{
            result = SentimentType.NODATA;
        }

        System.out.println(word+ " 의 posCount :" + posCount + " & negCount : " + negCount);
        return result;

    }
}
