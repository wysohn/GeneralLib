package org.generallib.pluginbase.manager.prompts;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.generallib.pluginbase.manager.prompts.PromptFacotry.ValueChanger;

public class EditPrompt extends IndexBasedPrompt<Map.Entry<String, Object>> {
    public EditPrompt(String title, Map<String, Object> value) {
        super(Prompt.END_OF_CONVERSATION, title, PromptFacotry.mapToEntryList(value));
    }

    public EditPrompt(Prompt parent, String title, Map<String, Object> value) {
        super(parent, title, PromptFacotry.mapToEntryList(value));
    }

    @Override
    public Prompt acceptInput(ConversationContext arg0, String arg1) {
        Prompt next = super.acceptInput(arg0, arg1);

        if(next == this) {
            if(arg1.length() > 0 && arg1.matches("[0-9]+")) {
                int index = Integer.parseInt(arg1);

                Entry<String, Object> pair = this.get(index);
                if (pair == null)
                    return this;

                return PromptFacotry.getEditPromptForValueType(this, pair.getKey(), new ValueChanger() {

                    @Override
                    public void onChange(Object newVal) {
                        pair.setValue(newVal);
                    }

                    @Override
                    public Object getValue() {
                        return pair.getValue();
                    }

                });
            }

            return this;
        }

        return next;
    }

    @Override
    public boolean blocksForInput(ConversationContext arg0) {
        return true;
    }

    @Override
    protected void print(Conversable conv) {
        super.print(conv);

        //TODO later user Language
        conv.sendRawMessage("Enter index to edit");
        conv.sendRawMessage("");


    }


}
