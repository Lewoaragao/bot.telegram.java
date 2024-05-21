package dev.bot.telegram.java.bots;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import dev.bot.telegram.java.Question;

public class QuizBot extends TelegramLongPollingBot {
	private Map<Long, List<Question>> userQuizzes = new HashMap<>();
	private Map<Long, Integer> userQuestionIndex = new HashMap<>();
	private Map<Long, List<Boolean>> userResults = new HashMap<>();

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
		if (update.hasMessage() && update.getMessage().hasText()) {
			handleTextMessage(update);
		} else if (update.hasCallbackQuery()) {
			handleCallbackQuery(update.getCallbackQuery());
		}
	}

	private void handleTextMessage(Update update) {
		Long chatId = update.getMessage().getChatId();
		String messageText = update.getMessage().getText();

		if (!userQuizzes.containsKey(chatId)) {
			sendCategoryOptions(chatId);
		} else {
			try {
				int answerIndex = Integer.parseInt(messageText) - 1;
				processAnswer(chatId, answerIndex);
			} catch (NumberFormatException e) {
				enviarMensagem(chatId, "Por favor, insira um número válido.");
			}
		}
	}

	private void sendCategoryOptions(Long chatId) {
		userQuizzes = new HashMap<>();
		userQuestionIndex = new HashMap<>();
		userResults = new HashMap<>();
		
		SendMessage message = new SendMessage();
		message.setChatId(chatId.toString());
		message.setText("Escolha uma categoria:");

		InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

		List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
		InlineKeyboardButton filmesButton = new InlineKeyboardButton();
		filmesButton.setText("Filmes");
		filmesButton.setCallbackData("categoria_Filmes");
		rowInline1.add(filmesButton);

		InlineKeyboardButton musicaButton = new InlineKeyboardButton();
		musicaButton.setText("Música");
		musicaButton.setCallbackData("categoria_Música");
		rowInline1.add(musicaButton);

		rowsInline.add(rowInline1);
		markupInline.setKeyboard(rowsInline);
		message.setReplyMarkup(markupInline);

		enviarMensagem(message);
	}

	private void handleCallbackQuery(CallbackQuery callbackQuery) {
		Long chatId = callbackQuery.getMessage().getChatId();
		String data = callbackQuery.getData();

		if (data.startsWith("categoria_")) {
			String categoria = data.split("_")[1];
			startQuiz(chatId, categoria);
		} else if (data.startsWith("resposta_")) {
			int respostaIndex = Integer.parseInt(data.split("_")[1]);
			processAnswer(chatId, respostaIndex);
		} else if (data.equals("start")) {
			sendCategoryOptions(chatId);
		}
	}

	private void startQuiz(Long chatId, String category) {
		List<Question> questions = loadQuestions(category);
		userQuizzes.put(chatId, questions);
		userQuestionIndex.put(chatId, 0);
		userResults.put(chatId, new ArrayList<>());

		sendQuestion(chatId);
	}

	private List<Question> loadQuestions(String category) {
		List<Question> questions = new ArrayList<>();

		if (category.equals("Filmes")) {
			questions.add(new Question("Quem dirigiu 'Pulp Fiction'?",
					List.of("Quentin Tarantino", "Steven Spielberg", "Martin Scorsese", "James Cameron"), 0));
			questions.add(new Question("Qual filme ganhou o Oscar de Melhor Filme em 2020?",
					List.of("1917", "Coringa", "Parasita", "Era uma vez em Hollywood"), 2));
			// Adicione mais perguntas aqui
		} else if (category.equals("Música")) {
			questions.add(new Question("Quem é o 'Rei do Pop'?",
					List.of("Michael Jackson", "Elvis Presley", "Freddie Mercury", "Prince"), 0));
			questions.add(new Question("Qual banda lançou o álbum 'The Wall'?",
					List.of("Pink Floyd", "The Beatles", "Led Zeppelin", "Queen"), 0));
			// Adicione mais perguntas aqui
		}

		return questions;
	}

	private void sendQuestion(Long chatId) {
		List<Question> questions = userQuizzes.get(chatId);
		int questionIndex = userQuestionIndex.get(chatId);

		if (questionIndex < questions.size()) {
			Question question = questions.get(questionIndex);
			String questionText = formatQuestion(question, questionIndex + 1);
			sendMessageWithOptions(chatId, questionText, question.getOptions());
		} else {
			endQuiz(chatId);
		}
	}

	private String formatQuestion(Question question, int questionNumber) {
		StringBuilder sb = new StringBuilder();
		sb.append("Pergunta ").append(questionNumber).append(":\n");
		sb.append(question.getQuestionText()).append("\n");
//		List<String> options = question.getOptions();
//		for (int i = 0; i < options.size(); i++) {
//			sb.append(i + 1).append(". ").append(options.get(i)).append("\n");
//		}
		return sb.toString();
	}

	private void sendMessageWithOptions(Long chatId, String questionText, List<String> options) {
		SendMessage message = new SendMessage();
		message.setChatId(chatId.toString());
		message.setText(questionText);

		InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

		for (int i = 0; i < options.size(); i++) {
			List<InlineKeyboardButton> rowInline = new ArrayList<>();
			InlineKeyboardButton button = new InlineKeyboardButton();
			button.setText(options.get(i));
			button.setCallbackData("resposta_" + i);
			rowInline.add(button);
			rowsInline.add(rowInline);
		}

		markupInline.setKeyboard(rowsInline);
		message.setReplyMarkup(markupInline);

		enviarMensagem(message);
	}

	private void processAnswer(Long chatId, int answerIndex) {
		List<Question> questions = userQuizzes.get(chatId);
		int questionIndex = userQuestionIndex.get(chatId);
		Question question = questions.get(questionIndex);

		boolean isCorrect = question.isCorrect(answerIndex);
		userResults.get(chatId).add(isCorrect);

		userQuestionIndex.put(chatId, questionIndex + 1);
		sendQuestion(chatId);
	}

	private void endQuiz(Long chatId) {
		List<Boolean> results = userResults.get(chatId);
		StringBuilder sb = new StringBuilder();
		sb.append("Quiz finalizado!\n");
		for (int i = 0; i < results.size(); i++) {
			sb.append("Pergunta ").append(i + 1).append(": ").append(results.get(i) ? "✅" : "❌").append("\n");
		}

//		sendMessage(chatId, sb.toString());

		// Cleanup
		userQuizzes.remove(chatId);
		userQuestionIndex.remove(chatId);
		userResults.remove(chatId);

		InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
		List<InlineKeyboardButton> rowInline1 = new ArrayList<>();

		InlineKeyboardButton musicaButton = new InlineKeyboardButton();
		musicaButton.setText("Reiniciar");
		musicaButton.setCallbackData("start");
		rowInline1.add(musicaButton);

		rowsInline.add(rowInline1);
		markupInline.setKeyboard(rowsInline);

		SendMessage message = new SendMessage();
		message.setReplyMarkup(markupInline);
		message.setChatId(chatId.toString());
		message.setText(sb.toString());

		enviarMensagem(message);
	}

//	@Override
	public void onCallbackQuery(CallbackQuery callbackQuery) {
		String callbackData = callbackQuery.getData();
		Long chatId = callbackQuery.getMessage().getChatId();

		if ("start".equals(callbackData)) {
			sendCategoryOptions(chatId);
		}
		// Adicione outros callbacks aqui, se necessário
	}

	private void enviarMensagem(Long chatId, String text) {
		SendMessage message = new SendMessage();
		message.setChatId(chatId.toString());
		message.setText(text);

		enviarMensagem(message);
	}

	public void enviarMensagem(SendMessage message) {
		try {
			execute(message);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
			TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
			botsApi.registerBot(new QuizBot());
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
}
