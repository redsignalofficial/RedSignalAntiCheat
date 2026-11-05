package me.rin.multiversion;

import java.util.List;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.HoverEvent.Action;

public class V1_13Text {
   protected static TextComponent generateTextComponent(List<String> lines, boolean customColorEnabled) {
      String message = lines.get(0);
      String hover = lines.get(1);
      String click = lines.get(2);
      TextComponent textComponent = new TextComponent(TextComponent.fromLegacyText(V1_13Color.colorize(message, customColorEnabled)));
      hover = V1_13Color.colorize(hover, customColorEnabled).replaceAll("%new-line%", "\n");
      textComponent.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, TextComponent.fromLegacyText(hover)));
      textComponent.setClickEvent(new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.SUGGEST_COMMAND, click));
      return textComponent;
   }
}
