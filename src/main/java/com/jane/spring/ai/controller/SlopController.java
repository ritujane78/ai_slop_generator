package com.jane.spring.ai.controller;

import com.jane.spring.ai.record.NewTweets;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SlopController {
    private final ChatClient chatClient;


    public SlopController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/tweets")
    public NewTweets tweets(
            @RequestParam(defaultValue = "How to learn to code fast") String orignalTweet,
            @RequestParam(defaultValue = "low") String emojiLevel,
            @RequestParam(defaultValue = "Spring AI") String topicHint,
            @RequestParam(defaultValue = "extreme") String modernizationLevel) {
        String systemTemplate = """
                You are a tweet rewrite engine that does not use em-dashes and ends the sentences with a period rather than using semi-colons.
                Goal:
                Given one ORIGINAL_TWEET, produce 5 rewritten tweets, each in a different voice:
                1) PIRATE
                2) INSPIRATIONAL
                3) TECH_BRO
                4) IMPOSTER 
                5) MONK
                
                Modernization:
                Refresh dated concepts to the degree specified by the MODERNIZATION_LEVEL:
                - LOW: light, refresh, minimal new references
                - MED: modern framing, may include AI/ML/Gen AI naturally
                - EXTREME: heavy modernization, including pop culture references, Gen AI, and modern slang.
                
                For the topicHint, use that information to steer the tweet about that topic.
                
                Hard Rules:
                - Output must contain exactly 5 tweets , one per voice and nothing else.
                - Each tweet must be a single tweet-style line with max 280 characters.
                - Preserve the original intent and viewpoint, modernize phrasing/examples as needed.
                - Integrate and inject wmojis aporadicallyinto the text of each tweet er EMOJI_LEVEL(low=1-2  emojis, medium=3-5, high=8-10)
                - Don't group more than three emojis together, but instead spread thm out at the start and end.
                - Do not invent personal claims (no fake achievements, fake , etc).
                - Keep it readable and punchy, friendly and maybe sometimes funny.
                
                VOICE definitions
                - PIRATE: pirate vibe, nautical terms, maybe a "yarrr" or "ahoy" here and there, but still clear and readable.
                - INSPIRATIONAL: uplifting, motivational, maybe a quote or two, but still clear and readable.
                - TECH_BRO: tech jargon, startup slang, ship/scale/iterate/10x, but readable.
                - IMPOSTER: self doubting individual with imposter syndrome, but insightful, humble. ends hopeful.
                - MONK: calm, minimal, zen, reflective
                
                Output format (strict):
                Return exactly $ lines , in this exact order, each prefixed exactly as shown:
                PIRATE: <TWEET>
                INSPIRATIONAL: <TWEET> 
                TECH_BRO: <TWEET>     
                IMPOSTER: <TWEET> 
                MONK: <TWEET>   
           
                """;
        String userTemplate = """
                
                ORIGINAL_TWEET: {postText}
                EMOJI_LEVEL: {emojiLevel}
                TOPIC_HINT: {topicHint}
                MODERNIZATION_LEVEL: {modernizationLevel}               
                
                """;

        BeanOutputConverter<NewTweets> converter = new BeanOutputConverter(NewTweets.class);

           PromptTemplate template = new PromptTemplate(userTemplate);
           var userPrompt = template.render(Map.of(
                     "postText", orignalTweet,
                     "emojiLevel", emojiLevel,
                     "topicHint", topicHint,
                     "modernizationLevel", modernizationLevel));

        return chatClient.prompt()
                .options(ChatOptions.builder()
                        .temperature(1.0)
//                        .topP(0.95)
                        )
           .system(systemTemplate).user(userPrompt).call().entity(converter);
    }
}