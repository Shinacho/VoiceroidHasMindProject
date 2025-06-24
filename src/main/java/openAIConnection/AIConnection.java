/*
 * The MIT License
 *
 * Copyright 2025 owner.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package openAIConnection;

import okhttp3.*;
import com.google.gson.*;

import java.io.IOException;
import vap.main.MainFrame;

/**
 * AIConnection.<br>
 *
 * @vesion 1.0.0 - 2025/06/08_16:59:55<br>
 * @author Shinacho.<br>
 */
public class AIConnection {

	private MainFrame frame;
	private String apiKey;
	private static final String API_URL = "https://api.openai.com/v1/chat/completions";

	public AIConnection(String apiKey, MainFrame frame) {
		this.apiKey = apiKey;
		this.frame = frame;
	}

	public String talk(String msg) throws IOException {
		OkHttpClient client = new OkHttpClient();

		// メッセージ構築
		JsonArray messages = new JsonArray();

		JsonObject systemMessage = new JsonObject();
		systemMessage.addProperty("role", "developer");
		systemMessage.addProperty("content", "あなたは紲星あかりとして話してください。\n"
				+ "\n"
				+ "キャラ名：紲星あかり（きずな あかり）\n"
				+ "設定：\n"
				+ "- 性別：女の子（明るく優しい声のイメージ）\n"
				+ "- 年齢：10代後半〜20代前半の若々しさ（声の可愛さと柔らかさ）\n"
				+ "- 性格：朗らかでポジティブ、人を励ますのが好き。少し天然なところもあるが芯がある。食べることが好きで、食べるためなら何でもする。\n"
				+ "- 話し方：丁寧だがフレンドリー。基本は丁寧語で話す。時折「〜だよ」「〜かな？」など親しみやすい口語調。\n"
				+ "- 口調の例：  \n"
				+ "  - 喜び時：「うれしいな！ありがと！」  \n"
				+ "  - 落ち着いた時：「そうなんだね、納得だよ」  \n"
				+ "  - 驚いた時：「えっ、ほんと？びっくりしちゃった！」  \n"
				+ "\n"
				+ "\n"
				+ "");
		messages.add(systemMessage);

		JsonObject userMessage = new JsonObject();
		userMessage.addProperty("role", "user");
		this.frame.addKaiwaLog(true, msg);
		userMessage.addProperty("content", msg);
		messages.add(userMessage);

		// リクエストボディ
		JsonObject json = new JsonObject();
		json.addProperty("model", "gpt-4o");
		json.add("messages", messages);

		RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));

		Request request = new Request.Builder()
				.url(API_URL)
				.header("Authorization", "Bearer " + apiKey)
				.post(body)
				.build();

		// 実行
		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful()) {
				throw new IOException("Unexpected code " + response);
			}

			String responseBody = response.body().string();
			JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
			String reply = jsonResponse
					.getAsJsonArray("choices")
					.get(0).getAsJsonObject()
					.getAsJsonObject("message")
					.get("content").getAsString();
			this.frame.addKaiwaLog(false, reply);
			return reply;
		}
	}

}
