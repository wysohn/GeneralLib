package org.generallib.pluginbase.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.chatlib.main.ChatLibAPI;
import org.chatlib.utils.chat.JsonMessage;
import org.chatlib.utils.chat.JsonMessagePlain;
import org.chatlib.utils.chat.handlers.JsonMessageClickEvent;
import org.chatlib.utils.chat.handlers.JsonMessageClickEvent.ClickAction;
import org.chatlib.utils.chat.handlers.JsonMessageHoverEvent;
import org.chatlib.utils.chat.handlers.JsonMessageHoverEvent.HoverAction;
import org.generallib.pluginbase.PluginAPISupport.APISupport;
import org.generallib.pluginbase.PluginBase;

public class ChatLibSupport extends APISupport {

    public ChatLibSupport(PluginBase base) {
        super(base);
    }

    @Override
    public void init() throws Exception {

    }

    private static final String jsonKey = "federations";
    public synchronized void setPrefix(Player player, Message[] prefixes){
        JsonMessagePlain[] msgs = toJsonMessage(prefixes);

        ChatLibAPI.getPrefixes(player).put(jsonKey, msgs);
    }

    public synchronized void resetPrefix(Player player){
        Map<String, JsonMessage[]> temp = ChatLibAPI.getPrefixes(player);
        if(temp == null)
            return;
        temp.remove(jsonKey);
    }

    public void send(Player player, Message[] messages){
        JsonMessagePlain[] jsonMessages = toJsonMessage(messages);

        ChatLibAPI.sendJsonMessage(player, ChatLibAPI.toJsonString(jsonMessages));
    }

    public void send(Player[] player, Message[] messages){
        JsonMessagePlain[] jsonMessages = toJsonMessage(messages);
        String jsonString = ChatLibAPI.toJsonString(jsonMessages);

        for(Player p : player){
            ChatLibAPI.sendJsonMessage(p, jsonString);
        }
    }

    private JsonMessagePlain[] toJsonMessage(Message[] messages){
        JsonMessagePlain[] jsonMessages = new JsonMessagePlain[messages.length];
        for(int i = 0; i < messages.length; i++){
            Message message = messages[i];

            JsonMessagePlain json = new JsonMessagePlain(message.string);
            if(message.click_OpenFile != null){
                json.setClickEvent(new JsonMessageClickEvent(ClickAction.open_file, message.click_OpenFile));
            }
            if(message.click_OpenUrl != null){
                json.setClickEvent(new JsonMessageClickEvent(ClickAction.open_url, message.click_OpenUrl));
            }
            if(message.click_RunCommand != null){
                json.setClickEvent(new JsonMessageClickEvent(ClickAction.run_command, message.click_RunCommand));
            }
            if(message.click_SuggestCommand != null){
                json.setClickEvent(new JsonMessageClickEvent(ClickAction.suggest_command, message.click_SuggestCommand));
            }
            if(message.hover_ShowText != null){
                json.setHoverEvent(new JsonMessageHoverEvent(HoverAction.show_text, message.hover_ShowText));
            }
            if(message.hover_ShowAchievement != null){
                json.setHoverEvent(new JsonMessageHoverEvent(HoverAction.show_achievement, message.hover_ShowAchievement));
            }
            if(message.hover_ShowItem != null){
                json.setHoverEvent(new JsonMessageHoverEvent(HoverAction.show_item, message.hover_ShowItem));
            }

            jsonMessages[i] = json;
        }

        return jsonMessages;
    }

    public static class MessageBuilder{
        private List<Message> messages = new ArrayList<>();
        private Message message;
        public MessageBuilder(String str){
            message = new Message(str);
            messages.add(message);
        }

        /**
         * This changes the current message to the specified and append it to the array. So once
         * you call this method, you lose the ability to build the previous message.
         * @param str string to append. Putting null in str does nothing.
         * @return the object itself.
         */
        public MessageBuilder append(String str){
            if(str == null)
                return this;

            message = new Message(str);
            messages.add(message);
            return this;
        }

        public MessageBuilder withClickOpenUrl(String value){
            message.resetClick();
            message.click_OpenUrl = value;
            return this;
        }

        public MessageBuilder withClickOpenFile(String value){
            message.resetClick();
            message.click_OpenFile = value;
            return this;
        }

        public MessageBuilder withClickRunCommand(String value){
            message.resetClick();
            message.click_RunCommand = value;
            return this;
        }

        public MessageBuilder withClickSuggestCommand(String value){
            message.resetClick();
            message.click_SuggestCommand = value;
            return this;
        }

        public MessageBuilder withHoverShowText(String value){
            message.resetHover();
            message.hover_ShowText = value;
            return this;
        }

        public MessageBuilder withHoverShowAchievement(String value){
            message.resetHover();
            message.hover_ShowAchievement = value;
            return this;
        }

        public MessageBuilder withHoverShowItem(String value){
            message.resetHover();
            message.hover_ShowItem = value;
            return this;
        }

        public Message[] build(){
            return messages.toArray(new Message[messages.size()]);
        }
    }

    public static class Message{
        private String string;

        private String click_OpenUrl;
        private String click_OpenFile;
        private String click_RunCommand;
        private String click_SuggestCommand;

        private String hover_ShowText;
        private String hover_ShowAchievement;
        private String hover_ShowItem;

        private Message(String str){
            this.string = str;
        }

        private void resetClick(){
            click_OpenUrl = null;
            click_OpenFile = null;
            click_RunCommand = null;
            click_SuggestCommand = null;
        }

        private void resetHover(){
            hover_ShowText = null;
            hover_ShowAchievement = null;
            hover_ShowItem = null;
        }
    }
}
