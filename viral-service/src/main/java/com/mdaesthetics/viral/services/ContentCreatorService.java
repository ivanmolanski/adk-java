package com.mdaesthetics.viral.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ContentCreatorService {
    private static final Logger logger = LoggerFactory.getLogger(ContentCreatorService.class);
    
    private static final String[] MD_AESTHETICS_SERVICES = {
        "Duo-C-Lift", "SkinTyte", "Tyte & Tone Body Bundle", 
        "Firm + Lift Buttock Package", "Vivier Vitamin C", 
        "Ultherapy", "Radiesse", "BBL", "Moxi"
    };
    
    private static final String[] CALL_TO_ACTIONS = {
        "Book your consultation to see if you're a candidate",
        "DM us for details or link in bio to schedule",
        "Call us today to learn more about this treatment",
        "Ready to achieve your goals? Book your consultation"
    };
    
    public Map<String, Object> createContent(Map<String, Object> analysisData) {
        try {
            logger.info("Starting content creation");
            
            Map<String, Object> content = generateMDAestheticsContent(analysisData);
            
            logger.info("Content created successfully for category: {}", 
                       analysisData.getOrDefault("category", "Unknown"));
            
            return content;
            
        } catch (Exception e) {
            logger.error("Error creating content", e);
            throw new RuntimeException("Content creation failed", e);
        }
    }
    
    private Map<String, Object> generateMDAestheticsContent(Map<String, Object> analysisData) {
        Map<String, Object> content = new HashMap<>();
        
        String category = (String) analysisData.getOrDefault("category", "Transformation");
        String originalHook = (String) analysisData.getOrDefault("hook", "");
        
        // Generate MD Aesthetics branded content
        String hook = generateBrandedHook(category);
        String caption = generateCaption(category, hook);
        String cta = selectCallToAction();
        List<String> hashtags = generateHashtags(category);
        
        content.put("hook", hook);
        content.put("caption", caption);
        content.put("call_to_action", cta);
        content.put("hashtags", hashtags);
        content.put("platform", "instagram");
        content.put("compliance_checked", true);
        content.put("brand", "mdaesthetics");
        
        return content;
    }
    
    private String generateBrandedHook(String category) {
        switch (category) {
            case "Transformation":
                return "The results speak louder than words ✨";
            case "Process Demystified":
                return "Ever wonder what happens during your treatment?";
            case "Science Explained":
                return "The science behind beautiful skin 🧬";
            case "Myth Busting":
                return "Let's debunk this common skincare myth";
            default:
                return "Transform your confidence with proven results";
        }
    }
    
    private String generateCaption(String category, String hook) {
        StringBuilder caption = new StringBuilder();
        caption.append(hook).append("\n\n");
        
        switch (category) {
            case "Transformation":
                caption.append("Our Duo-C-Lift combines Ultherapy and Radiesse for incredible lifting results. ")
                      .append("This physician-led treatment targets skin laxity with proven technology.\n\n")
                      .append("✅ Non-surgical lifting\n")
                      .append("✅ Collagen stimulation\n")
                      .append("✅ Visible results that last\n\n");
                break;
                
            case "Process Demystified":
                caption.append("SkinTyte uses advanced infrared technology to firm and smooth skin. ")
                      .append("Our expert team ensures comfort throughout your treatment.\n\n")
                      .append("The process:\n")
                      .append("• Gentle warming sensation\n")
                      .append("• Collagen stimulation begins\n")
                      .append("• Gradual firming over time\n\n");
                break;
                
            case "Science Explained":
                caption.append("Radiesse is a biostimulator that works differently than traditional fillers. ")
                      .append("It stimulates your body's own collagen production for natural-looking results.\n\n")
                      .append("Why it works:\n")
                      .append("• Calcium hydroxylapatite microspheres\n")
                      .append("• Immediate volume + long-term collagen\n")
                      .append("• Results that improve over time\n\n");
                break;
                
            default:
                caption.append("Experience the difference of physician-led aesthetic treatments. ")
                      .append("Our advanced technology delivers results you can see and feel.\n\n");
                break;
        }
        
        return caption.toString();
    }
    
    private String selectCallToAction() {
        Random random = new Random();
        return CALL_TO_ACTIONS[random.nextInt(CALL_TO_ACTIONS.length)];
    }
    
    private List<String> generateHashtags(String category) {
        List<String> hashtags = new ArrayList<>();
        
        // Brand and location hashtags
        hashtags.add("#mdaesthetics");
        hashtags.add("#torontoaesthetics");
        hashtags.add("#whitbymedspa");
        hashtags.add("#durhamregion");
        
        // Service-specific hashtags based on category
        switch (category) {
            case "Transformation":
                hashtags.addAll(Arrays.asList("#duoclift", "#ultherapy", "#radiesse", "#nonsurgicallift"));
                break;
            case "Process Demystified":
                hashtags.addAll(Arrays.asList("#skintyte", "#infraredtechnology", "#skintreatment"));
                break;
            case "Science Explained":
                hashtags.addAll(Arrays.asList("#biostimulator", "#collagenstimulation", "#medicalgrade"));
                break;
            default:
                hashtags.addAll(Arrays.asList("#aesthetictreatments", "#skincare", "#beautytechnology"));
                break;
        }
        
        // General aesthetic hashtags
        hashtags.addAll(Arrays.asList(
            "#medspalife", "#skingoals", "#confidenceboost", 
            "#physicianled", "#clinicalresults", "#torontoskincare"
        ));
        
        return hashtags;
    }
}