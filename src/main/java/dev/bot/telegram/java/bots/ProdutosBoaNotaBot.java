package dev.bot.telegram.java.bots;

import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class ProdutosBoaNotaBot extends TelegramLongPollingBot {
	private String imagePath;
	private String nota;
	private String valor;
	private String fornecedor;

	@Override
	public String getBotUsername() {
		return "YOUR_BOT_USER";
	}

	@Override
	public String getBotToken() {
		return "YOUR_BOT_TOKEN";
	}

	@Override
	public void onUpdateReceived(Update update) {
		if (update.hasMessage() && update.getMessage().hasPhoto()) {
			imagePath = update.getMessage().getPhoto().get(0).getFileId();
			sendMessage(update.getMessage().getChatId(), "Qual nota?");
		} else if (update.hasMessage() && update.getMessage().hasText()) {
			handleTextMessage(update.getMessage());
		} else if (update.hasCallbackQuery()) {
			onCallbackQuery(update.getCallbackQuery());
		}
	}

	private void handleTextMessage(Message message) {
		String text = message.getText();
		Long chatId = message.getChatId();

		if (nota == null) {
			nota = text;
			sendMessage(chatId, "Qual valor?");
		} else if (valor == null) {
			valor = text;
			sendMessage(chatId, "Qual fornecedor?");
		} else if (fornecedor == null) {
			fornecedor = text;
			sendMessage(chatId, "Qual link?");
		} else {
			String link = text;
			sendConfirmationMessage(chatId, link);
		}
	}

	private void sendMessage(Long chatId, String text) {
		SendMessage message = new SendMessage();
		message.setChatId(chatId.toString());
		message.setText(text);

		try {
			execute(message);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	private void sendConfirmationMessage(Long chatId, String link) {
		SendMessage message = new SendMessage();
		message.setChatId(chatId.toString());
		message.setText("Envie a imagem com as informações?");

		InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
		List<InlineKeyboardButton> rowInline = new ArrayList<>();

		InlineKeyboardButton button = new InlineKeyboardButton();
		button.setText("Enviar");
		button.setCallbackData("SEND_IMAGE");

		rowInline.add(button);
		rowsInline.add(rowInline);
		markupInline.setKeyboard(rowsInline);
		message.setReplyMarkup(markupInline);

		try {
			execute(message);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

//	@Override
	public void onCallbackQuery(CallbackQuery callbackQuery) {
		if ("SEND_IMAGE".equals(callbackQuery.getData())) {
			sendImage(callbackQuery.getMessage().getChatId());
		}
	}

	private void sendImage(Long chatId) {
		SendPhoto msg = new SendPhoto();
		msg.setChatId(chatId.toString());
		msg.setPhoto(new InputFile(imagePath));
		msg.setCaption("Nota: " + nota + "\nValor: " + valor + "\nFornecedor: " + fornecedor);

		InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
		List<InlineKeyboardButton> rowInline = new ArrayList<>();

		InlineKeyboardButton button = new InlineKeyboardButton();
		button.setText("Link");
		button.setUrl("YOUR_LINK_HERE"); // Substitua "YOUR_LINK_HERE" pelo link real

		rowInline.add(button);
		rowsInline.add(rowInline);
		markupInline.setKeyboard(rowsInline);
		msg.setReplyMarkup(markupInline);

		try {
			execute(msg);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
			TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
			botsApi.registerBot(new ProdutosBoaNotaBot());
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
}
