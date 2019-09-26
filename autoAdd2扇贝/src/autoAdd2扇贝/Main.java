package autoAdd2�ȱ�;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class Main {

	public static void main(String[] args) throws ClientProtocolException, IOException {
		CloseableHttpClient httpClient = HttpClients.custom().setDefaultCookieStore(new BasicCookieStore())
				.setRedirectStrategy(new LaxRedirectStrategy()).build();
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(5000).setConnectionRequestTimeout(1000)
				.setSocketTimeout(5000).build();

		// �����ȱ�Ӣ���¼���棬cookie���Զ�����httpClient
		HttpGet httpGet = new HttpGet(
				"http://pass.sdu.edu.cn/cas/login?service=http://bkjws.sdu.edu.cn/f/j_spring_security_thauth_roaming_entry");
		httpClient.execute(httpGet);

		HttpPost login = new HttpPost("https://apiv3.shanbay.com/bayuser/login");
		// ��д�˻�����
		StringEntity requestEntity = new StringEntity(
				"{\"account\":\"15105375795\",\"password\":\"123456\",\"code_2fa\":\"\"}", "utf-8");
		requestEntity.setContentEncoding("UTF-8");
		login.setEntity(requestEntity);
		// ��������ͷ
		login.addHeader("Content-Type", "application/json");
		CloseableHttpResponse response = httpClient.execute(login);
		// �鿴�Ƿ��¼�ɹ�,���ɹ����˳�����
		System.out.println(response.getStatusLine().getStatusCode());
		if (response.getStatusLine().getStatusCode() != 200) {
			System.exit(0);
		}

		// �Զ���ӵ��ʵ��ȱ����ʱ�
		Set<String> words = getWords();
		for (String word : words) {
			HttpGet sGet = new HttpGet(
					"https://www.shanbay.com/api/v1/bdc/search/?version=2&word=" + word + "&_=1569255102786");
			sGet.setConfig(requestConfig);
			response = httpClient.execute(sGet);
			String content = EntityUtils.toString(response.getEntity());
			JSONObject jsonObj = JSON.parseObject(content);
			int status_code = jsonObj.getInteger("status_code");
			if (status_code != 0) {
				System.out.println("����" + word + "δ�鵽");
				continue;
			}
			String id = jsonObj.getJSONObject("data").getString("id");
			HttpPost sPost = new HttpPost("https://www.shanbay.com/api/v1/bdc/learning/");
			StringEntity sEntity = new StringEntity("{\"id\":" + id + ",\"content_type\":\"vocabulary\"}", "utf-8");
			sEntity.setContentEncoding("UTF-8");
			sPost.setEntity(sEntity);
			sPost.addHeader("Content-Type", "application/json");
			httpClient.execute(sPost);
			sGet.abort();
			sPost.abort();
		}

	}

	public static Set<String> getWords() throws IOException {
		Set<String> words = new HashSet<>();
		BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\��\\ѧϰ����\\�ٿ�һ��Ӣ��\\����20190925.txt"));
		String data = "";
		String line = null;
		while ((line = reader.readLine()) != null) {
			data += line + " ";
		}
		reader.close();
		String pattern = "[a-zA-Z]+-*[a-zA-Z]*";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(data);

		while (m.find()) {
			words.add(m.group());
		}
		return words;
	}
}
