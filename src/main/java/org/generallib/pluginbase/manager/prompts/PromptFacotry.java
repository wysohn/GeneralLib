package org.generallib.pluginbase.manager.prompts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.conversations.BooleanPrompt;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

public class PromptFacotry {
    static <K> Prompt getEditPromptForValueType(EditPromptBase parent, K title, PromptFacotry.ValueChanger changer) {
        Object value = changer.getValue();

        if(value instanceof Number) {
            return new NumericPrompt() {

                @Override
                public String getPromptText(ConversationContext arg0) {
                    return "Enter the number";
                }

                @Override
                protected Prompt acceptValidatedInput(ConversationContext arg0, Number arg1) {
                    changer.onChange(arg1);
                    return parent;
                }

            };
        } else if(value instanceof Boolean) {
            return new BooleanPrompt() {

                @Override
                public String getPromptText(ConversationContext arg0) {
                    return "Enter 'true' or 'false'";
                }

                @Override
                protected Prompt acceptValidatedInput(ConversationContext arg0, boolean arg1) {
                    Boolean box = arg1;
                    changer.onChange(box);
                    return parent;
                }

            };
        } else if(value instanceof String) {
            return new StringPrompt() {
                @Override
                public String getPromptText(ConversationContext arg0) {
                    return "Enter the value";
                }

                @Override
                public Prompt acceptInput(ConversationContext arg0, String arg1) {
                    changer.onChange(arg1);
                    return parent;
                }

            };
        } else if(value instanceof Map) {
            return new EditPrompt(parent, String.valueOf(title), (Map<String, Object>) value);
        } else if(value instanceof List) {
            return new ListEditPrompt(parent, String.valueOf(title), (List<String>) value);
        }

        return null;
    }

    interface ValueChanger{
        void onChange(Object newVal);
        Object getValue();
    }

    static <K, V> List<Map.Entry<K, V>> mapToEntryList(Map<K, V> map){
        List<Map.Entry<K, V>> list = new ArrayList<>();
        for(Map.Entry<K, V> entry : map.entrySet()) {
            list.add(entry);
        }
        return list;
    }
}
