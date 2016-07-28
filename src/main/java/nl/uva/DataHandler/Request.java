/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.uva.DataHandler;

import java.io.IOException;
import java.util.HashSet;
import nl.uva.lm.LanguageModel;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Mostafa Dehghani
 */
public class Request {
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Request.class.getName());
    
    public Double nuteralRate = 2D;
    public String pId;
    public String reqId;
    public String pGender;
    public String pAge;
    public String group;
    public String season ;
    public String tripType;
    public String duration;
    public String locationId;
    
    public HashSet<RatedDoc> ratedPrefrences;
    public HashSet<RatedDoc> ratedCandidates;
    public HashSet<String> suggestionCandidates;
    
    public String PositiveQueryString;
    public String NegativeQueryString;
    
    public String PositiveTagsString;
    public String NegativeTagsString;
    
    public String PositiveAllString;
    
    public LanguageModel userPositiveMixturePLM;
    public LanguageModel userNegativeMixturePLM;
    
    public LanguageModel userPositiveMixtureTags;
    public LanguageModel userNegativeMixtureTags; 
    public LanguageModel userPrefPositiveMixtureLM;
    public LanguageModel userCandPositiveMixtureLM;
    

    
//    public Request(String pId, String pGender, String pAge, String reqId, String group, 
//            String season, String tripType, String duration, String locationId, 
//            HashSet<RatedDoc> ratedPrefrences, HashSet<RatedDoc> ratedCandidates,
//            HashSet<String> suggestionCandidates) throws IOException {
//        this.pId = pId;
//        this.reqId = reqId;
//        this.pGender = pGender;
//        this.pAge = pAge;
//        this.reqId = reqId;
//        this.group = group;
//        this.season = season;
//        this.tripType = tripType;
//        this.duration = duration;
//        this.locationId = locationId;
//        this.ratedPrefrences = ratedPrefrences;
//        this.ratedCandidates = ratedCandidates;
//        this.suggestionCandidates = suggestionCandidates;
////        this.generateUserQuery();
//        //        this.generateUserLM();
//    }
    
    
    public Request(String pId, String pGender, String pAge, String reqId, String group, 
            String season, String tripType, String duration, String locationId, 
            HashSet<RatedDoc> ratedPrefrences) throws IOException {
        this.pId = pId;
        this.reqId = reqId;
        this.pGender = pGender;
        this.pAge = pAge;
        this.reqId = reqId;
        this.group = group;
        this.season = season;
        this.tripType = tripType;
        this.duration = duration;
        this.locationId = locationId;
        this.ratedPrefrences = ratedPrefrences;
        
    }

    
    
    private void generateUserQuery() throws IOException{
        String PositiveQuery = "";
        String NegativeQuery = "";
        String PositiveTags = "";
        String NegativeTags = "";
        String PositiveAll = this.duration + " " + this.group + " " + this.pGender + " " + this.season + " " +  this.tripType;
        for(RatedDoc rd : this.ratedPrefrences){
            if(rd.rate == -1 || rd.rate == 2)
                continue;
            String qStr = rd.text + " ";
            String tagStr = StringUtils.join(rd.tags, " ") + " ";
            if(rd.rate > 2){
                if(rd.rate == 4.0)
                    rd.setNewRate(2.0);
                if(rd.rate == 3.0)
                    rd.setNewRate(1.0);
                PositiveQuery += StringUtils.repeat(qStr, rd.rate.intValue());
                PositiveTags += StringUtils.repeat(tagStr, rd.rate.intValue());
            }
            else if(rd.rate < 2){
                if(rd.rate == 0.0)
                    rd.setNewRate(2.0);
                if(rd.rate == 1.0)
                    rd.setNewRate(1.0);
                NegativeQuery += StringUtils.repeat(qStr, rd.rate.intValue());
                NegativeTags += StringUtils.repeat(tagStr, rd.rate.intValue());
            }
        }
        this.PositiveQueryString = PositiveQuery;
        this.NegativeQueryString = NegativeQuery;
        this.PositiveTagsString = PositiveTags;
        this.NegativeTagsString = NegativeTags;
        this.PositiveAllString = StringUtils.repeat(PositiveAll, 5) + PositiveQuery + PositiveTags;
    }
    
    
    
    
    public String getPositiveQuery() throws IOException {
        if(this.PositiveQueryString == null)
            this.generateUserQuery();
        return this.PositiveQueryString;
    }
    public String getNegativeQuery() throws IOException {
        if(this.NegativeQueryString == null)
            this.generateUserQuery();
        return this.NegativeQueryString;
    }
    
    public String getPositiveTags() throws IOException {
        if(this.PositiveTagsString == null)
            this.generateUserQuery();
        return this.PositiveTagsString;
    }
    public String getNegativeTags() throws IOException {
        if(this.NegativeTagsString == null)
            this.generateUserQuery();
        return this.NegativeTagsString;
    }

    public String getPositiveAll() throws IOException {
        if(this.PositiveAllString == null)
            this.generateUserQuery();
        return this.PositiveAllString;
    }
    
    
    public LanguageModel getUserPrefPositiveMixtureLM() throws IOException {
//        if(this.userPrefPositiveMixtureLM == null)
//            this.generateUserPrefLM();
        return userPrefPositiveMixtureLM;
    }

    public LanguageModel getUserCandPositiveMixtureLM() throws IOException {
//        if(this.userCandPositiveMixtureLM == null)
//            this.generateUserCandLM();
        return userCandPositiveMixtureLM;
    }
    
//    private void generateUserPrefLM() throws IOException{
//        HashSet<RatedDoc> positivePrefrences = new HashSet<>();
//        HashSet<RatedDoc> negativePrefrences = new HashSet<>();
//        Double sumPR =0D;
//        Double sumNR =0D;
//        for(RatedDoc p : this.ratedPrefrences){
//           
//            if(p.rate == -1D /*not seen*/ || p.rate == this.nuteralRate)
//                continue;
//            
//            if(p.rate > 2){
//                if(p.rate == 4.0)
//                    p.setNewRate(2.0);
//                if(p.rate == 3.0)
//                    p.setNewRate(1.0);
//                positivePrefrences.add(p);
//                sumPR += p.rate;
//            }
//            
//            else if(p.rate < 2){
//                if(p.rate == 0.0)
//                    p.setNewRate(2.0);
//                if(p.rate == 1.0)
//                    p.setNewRate(1.0);
//                negativePrefrences.add(p);
//                sumNR += p.rate;
//            }
//        }
//        this.userPrefPositiveMixtureLM = MixtureModel(sumPR, positivePrefrences);
//        this.userNegativeMixturePLM = new ParsimoniousMixtureModel(sumNR, negativePrefrences);
////        this.userPositiveMixturePLM = new ParsimoniousMixtureModel(sumPR, positivePrefrences);
////        this.userNegativeMixturePLM = new ParsimoniousMixtureModel(sumNR, negativePrefrences);
////        this.userPositiveMixtureTags = new MixtureTags(sumPR, positivePrefrences);
////        this.userNegativeMixtureTags = new MixtureTags(sumNR, negativePrefrences);
//    }
//    private void generateUserCandLM() throws IOException{
//        HashSet<RatedDoc> positiveCandidates = new HashSet<>();
//        HashSet<RatedDoc> negativePrefrences = new HashSet<>();
//        Double sumPR =0D;
//        Double sumNR =0D;
//        for(RatedDoc c : this.ratedCandidates){
//           
//            if(c.rate == -1 || c.rate == 2)
//                continue;
//            
//            if(c.rate > 2){
//                if(c.rate == 4.0)
//                    c.setNewRate(2.0);
//                if(c.rate == 3.0)
//                    c.setNewRate(1.0);
//                positiveCandidates.add(c);
//                sumPR += c.rate;
//            }
//            
//            else if(c.rate < 2){
//                if(c.rate == 0.0)
//                    c.setNewRate(2.0);
//                if(c.rate == 1.0)
//                    c.setNewRate(1.0);
//                negativePrefrences.add(c);
//                sumNR += c.rate;
//            }
//        }
//                }
//        this.userCandPositiveMixtureLM = this.MixtureModel(sumPR, positiveCandidates);
//        this.userNegativeMixturePLM = new ParsimoniousMixtureModel(sumNR, negativePrefrences);
//    }
    
//    private void generateUserLM() throws IOException{
//        HashSet<Prefrence> positivePrefrences = new HashSet<>();
//        HashSet<Prefrence> negativePrefrences = new HashSet<>();
//        Double sumPR =0D;
//        Double sumNR =0D;
//        for(RatedDoc p : this.ratedPrefrences){
//           
//            if(p.rate == -1 || p.rate == 2)
//                continue;
//            
//            if(p.rate > 2){
//                if(p.rate == 4.0)
//                    p.setNewRate(2.0);
//                if(p.rate == 3.0)
//                    p.setNewRate(1.0);
//                positivePrefrences.add(p);
//                sumPR += p.rate;
//            }
//            
//            else if(p.rate < 2){
//                if(p.rate == 0.0)
//                    p.setNewRate(2.0);
//                if(p.rate == 1.0)
//                    p.setNewRate(1.0);
//                negativePrefrences.add(p);
//                sumNR += p.rate;
//            }
//        }
//        this.userPositiveMixturePLM = new ParsimoniousMixtureModel(sumPR, positivePrefrences);
//        this.userNegativeMixturePLM = new ParsimoniousMixtureModel(sumNR, negativePrefrences);
//        this.userPositiveMixtureTags = new MixtureTags(sumPR, positivePrefrences);
//        this.userNegativeMixtureTags = new MixtureTags(sumNR, negativePrefrences);
//    }
}
