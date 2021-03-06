package org.generallib.pluginbase.manager.prompts;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.generallib.pluginbase.PluginBase;
import org.generallib.pluginbase.PluginLanguage.Language;
import org.generallib.pluginbase.language.DefaultLanguages;
import org.generallib.pluginbase.manager.prompts.PromptFacotry.ValueChanger;

public class EditPrompt extends IndexBasedPrompt<Map.Entry<Language, Object>> {
    public EditPrompt(PluginBase base, Object title, Map<Language, Object> value) {
        super(base, Prompt.END_OF_CONVERSATION, title, PromptFacotry.mapToEntryList(value));
    }

    public EditPrompt(PluginBase base, Prompt parent, Object title, Map<Language, Object> property) {
        super(base, parent, title, PromptFacotry.mapToEntryList(property));
    }

    @Override
    public Prompt acceptInput(ConversationContext arg0, String arg1) {
        Prompt next = super.acceptInput(arg0, arg1);

        if(next == this) {
            if(arg1.length() > 0 && arg1.matches("[0-9]+")) {
                int index = Integer.parseInt(arg1);

                Entry<Language, Object> pair = this.get(index);
                if (pair == null)
                    return this;

                return PromptFacotry.getEditPromptForValueType(base, this, pair.getKey(), new ValueChanger() {

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

        conv.sendRawMessage(base.lang.parseFirstString(conv, DefaultLanguages.General_PromptMain_EnterIndex));
        conv.sendRawMessage("");


    }


}
