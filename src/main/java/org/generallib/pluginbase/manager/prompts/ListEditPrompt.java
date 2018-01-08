package org.generallib.pluginbase.manager.prompts;

import java.util.List;

import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

public class ListEditPrompt extends IndexBasedPrompt<String>{
    public ListEditPrompt(Prompt parent, String title, List<String> currentData) {
        super(parent, title, currentData);
    }

    public ListEditPrompt(String title, List<String> currentData) {
        super(Prompt.END_OF_CONVERSATION, title, currentData);
    }

    @Override
    public Prompt acceptInput(ConversationContext arg0, String arg1) {
        Prompt next = super.acceptInput(arg0, arg1);

        if (next == this) {
            if (arg1.startsWith("add") && arg1.length() > 4) {
                String value = arg1.substring(3).trim();

                if (!value.isEmpty())
                    this.add(value);
            } else if (arg1.startsWith("del") && arg1.length() > 4) {
                String value = arg1.substring(3).trim();

                int index = -1;
                try {
                    index = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    return this;
                }

                this.delete(index);
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
        conv.sendRawMessage("add <value> - add <value> to the list");
        conv.sendRawMessage("del <num> - delete data at <num> index");
    }
}
