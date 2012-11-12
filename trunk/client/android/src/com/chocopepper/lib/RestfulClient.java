package com.chocopepper.lib;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import android.content.Context;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.chocopepper.chococam.util.Logger;


public class RestfulClient {
	
	public static class MySSLSocketFactory extends SSLSocketFactory {
	    SSLContext sslContext = SSLContext.getInstance("TLS");

	    public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
	        super(truststore);

	        TrustManager tm = new X509TrustManager() {
	            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	            }

	            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	            }

	            public X509Certificate[] getAcceptedIssuers() {
	                return null;
	            }
	        };

	        sslContext.init(null, new TrustManager[] { tm }, null);
	    }

	    @Override
	    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
	        return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
	    }

	    @Override
	    public Socket createSocket() throws IOException {
	        return sslContext.getSocketFactory().createSocket();
	    }
	}
	
	
	public DefaultHttpClient getNewHttpClient(Context ctx, int timeout) {
	    try {
	        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
	        trustStore.load(null, null);

	        SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
	        sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

	        HttpParams params = new BasicHttpParams();
	        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
	        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

	        SchemeRegistry registry = new SchemeRegistry();
	        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	        registry.register(new Scheme("https", sf, 443));

	        ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
	        
	        

			mTimeout = timeout;
			
			// 2012-10-09 brucewang
			// 속도 향상.
			params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
			
			
			HttpConnectionParams.setConnectionTimeout(params, mTimeout);
			int timeoutSocket = timeout;
			HttpConnectionParams.setSoTimeout(params, timeoutSocket);
			HttpConnectionParams.setSocketBufferSize(params, 1024 * 64);

			CookieSyncManager.createInstance(ctx);
			cookieManager = CookieManager.getInstance();
			CookieSyncManager.getInstance().startSync();

			
	        return new DefaultHttpClient(ccm, params);
	    } catch (Exception e) {
	        return new DefaultHttpClient();
	    }
	}
	
	private static final String TAG = Logger.makeLogTag(RestfulClient.class);
	private static int DEFAULT_TIMEOUT = 100000;
	
	
	private static String mUserId = "";
	public void setUserId(String str){
		mUserId = str;
	}
	private static String mDeviceId = "";
	public void setDeviceId(String str){
		mDeviceId = str;
	}

	// =====================================================================
	// Convert HttpResponse information to String.
	// =====================================================================
	public static String getStringFromHttpResponse(HttpResponse httpResponse)
			throws IOException {
		String response = IOUtils.toString(httpResponse.getEntity().getContent(),"UTF-8");
		httpResponse.getEntity().consumeContent();
		return response;
		
		
//		HttpEntity entity = httpResponse.getEntity();
//		if (entity == null)
//			return null;
//		
//		Logger.e(TAG, "> getStringFromHttpResponse START");
//
//		InputStream is = null;
//		StringBuilder sb = new StringBuilder();
//		try {
//			is = entity.getContent();
//			BufferedReader reader = new BufferedReader(
//					new InputStreamReader(is));
//			String line = null;
//			while ((line = reader.readLine()) != null) {
//				sb.append(line + "\n");
//			}
//
//			is.close();
//			entity.consumeContent();
//		} catch (IllegalStateException e1) {
//			e1.printStackTrace();
//			throw e1;
//		} catch (IOException e1) {
//			e1.printStackTrace();
//
//			throw e1;
//		}
//		
//		Logger.e(TAG, "> getStringFromHttpResponse END");
//
//		return sb.toString();
	}

	@SuppressWarnings("unused")
	private Context mCtx;

	private static boolean bLoggedin = false;

	public static boolean loggedin() {
		return bLoggedin;
	}

	public static void setLoggedin(boolean b) {
		bLoggedin = b;
	}

	public enum HttpReqType {
		HTTP_GET, HTTP_POST, HTTP_PUT, HTTP_DEL
	}

	private ArrayList<NameValuePair> params;
	private ArrayList<NameValuePair> headers;
	private int responseCode;
	private String responseMessage;
	private String responseBody;

	public String getResponse() {
		return responseBody;
	}

	public String getErrorMessage() {
		return responseMessage;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public CookieManager cookieManager;
	private int mTimeout = DEFAULT_TIMEOUT;

	public RestfulClient(Context ctx){
		this(ctx, DEFAULT_TIMEOUT);
	}
	// private Object mSyncObject;
	public RestfulClient(Context ctx, int timeout) {
		params = new ArrayList<NameValuePair>();
		headers = new ArrayList<NameValuePair>();
		// mSyncObject = new Object();

		mCtx = ctx;

		
		
		
		mHttpClient = getNewHttpClient(ctx, timeout);
		//mHttpClient = new DefaultHttpClient(httpParameters);

		// 2010-10-05 brucewang
		// Prevent Http redirect to start automatically.
		mHttpClient.setRedirectHandler(new RedirectHandler() {
			@Override
			public boolean isRedirectRequested(HttpResponse response,
					HttpContext context) {
				return false;
			}

			@Override
			public URI getLocationURI(HttpResponse response, HttpContext context)
					throws ProtocolException {
				return null;
			}
		});
	}

	public void AddParam(String name, String value) {
		// synchronized (mSyncObject) {
		params.add(new BasicNameValuePair(name, value));
		// }
	}

	public void AddHeader(String name, String value) {
		// synchronized (mSyncObject) {
		headers.add(new BasicNameValuePair(name, value));
		// }
	}

	FileEntity mFileEntity = null;

	public void AddFileEntity(File data) {
		mFileEntity = new FileEntity(data, "binary/octet-stream");
	}

	/**
	 * 2010-10-05 brucewang CAUTION If you create new instance, the session
	 * information is not share between those instances.
	 */
	private DefaultHttpClient mHttpClient;

	private static List<Cookie> mCookies = null;
	
	

	
	private boolean mbUseHttpAuth = false;
	private String  mstrAuthScopeHost = "";
	private int     miAuthScopePort = 0;
	private String  mstrAuthUsername = "";
	private String  mstrAuthPassword = "";
	public void setAuthInfo(String strHost, int port, String strUser, String strPass){
		mbUseHttpAuth = true;
		mstrAuthScopeHost = strHost;
		miAuthScopePort = port;
		mstrAuthUsername = strUser;
		mstrAuthPassword = strPass;
	}
	

	
	private String mStringEntity="";
	public void setStringEntity(String str){
		mStringEntity = str;
	}
	/**
	 * 
	 * @param iRequestType
	 * @param strUrl
	 * @return HTTP resonse code -1 means "not supported HTTP method".
	 */
	public int sendHttpRequest(HttpReqType iRequestType, String strUrl) {
		Logger.e(TAG, "> sendHttpRequest START");
		if( mUserId!=null && mUserId.length()>0 ){
			AddHeader("user_id", mUserId);
		}
//		if( mDeviceId!=null && mDeviceId.length()>0 ){
//			AddHeader("device_id", mDeviceId);
//		}
		
		// 2012-06-09 brucewang
		// 기본  HTTP Auth 기능 추가.
		if(mbUseHttpAuth){
			mHttpClient.getCredentialsProvider().setCredentials(
			        new AuthScope(mstrAuthScopeHost, miAuthScopePort), 
			        new UsernamePasswordCredentials(mstrAuthUsername, mstrAuthPassword));
			mbUseHttpAuth=false;
		}
		

		// synchronized (mSyncObject) {
		HttpResponse httpResponse = null;

		
		URL url;
		try {
			url = new URL(strUrl);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			return -1;
		}

		// String strReqPath = url.getPath();
		// if(strReqPath!=null){
		// ActivityMain pMain = ActivityMain.getInstance();
		// if(pMain!=null){
		// pMain.googleAnalyticsTrackPageView(strReqPath);
		// }
		// }

		if (mCookies != null) {
			CookieStore cookieStore = ((DefaultHttpClient) mHttpClient)
					.getCookieStore();
			for (Cookie cookie : mCookies) {
				cookieStore.addCookie(cookie);
			}
		}

		// add parameters
		String combinedParams = "";
		if (!params.isEmpty()) {
			combinedParams += "?";
			for (NameValuePair p : params) {
				try {
					String paramString;
					paramString = p.getName() + "="
							+ URLEncoder.encode(p.getValue(), "UTF-8");
					if (combinedParams.length() > 1) {
						combinedParams += "&" + paramString;
					} else {
						combinedParams += paramString;
					}
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					return -1;
				}
			}
		}
		
		Logger.e(TAG, "Header - " + headers.toString());
		Logger.e(TAG, "Params - " + params.toString());
		Logger.e(TAG, "URL - " + strUrl);
		
		// 2012-11-06 brucewang
		// send locale, useragent...
		Locale locale = mCtx.getResources().getConfiguration().locale;
		String strLocale = locale.toString();
		String userAgent = System.getProperty( "http.agent" );
		mHttpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, userAgent);

		try {
			switch (iRequestType) {
				case HTTP_GET :
					// URL with additional parameters
					HttpGet requestGet = new HttpGet(strUrl + combinedParams);
					
					requestGet.setHeader("Cache-Control", "no-cache");
					requestGet.setHeader("Locale", strLocale);
					
					// add headers
					for (NameValuePair h : headers) {
						requestGet.addHeader(h.getName(), h.getValue());
					}
					httpResponse = mHttpClient.execute(requestGet);
					break;
				case HTTP_POST :
					HttpPost requestPost = new HttpPost(strUrl);
					requestPost.setHeader("Cache-Control", "no-cache");
					requestPost.setHeader("Locale", strLocale);

					// add parameters
					if (!params.isEmpty()) {
						HttpEntity entity = new UrlEncodedFormEntity(params,
								"UTF-8");
						requestPost.setEntity(entity);
					}

					// add headers
					for (NameValuePair h : headers) {
						requestPost.addHeader(h.getName(), h.getValue());
					}

					httpResponse = mHttpClient.execute(requestPost);
					break;

				case HTTP_PUT :
					HttpPut requestPut = new HttpPut(strUrl);// +
																// combinedParams);
					requestPut.setHeader("Cache-Control", "no-cache");
					requestPut.setHeader("Locale", strLocale);
					if (!params.isEmpty()) {
						HttpEntity entity = new UrlEncodedFormEntity(params,"UTF-8");
						requestPut.setEntity(entity);
					}
//					// 2010-12-02 brucewang
//					// FileUpload test..
//					if (mFileEntity != null) {
//						requestPut.setEntity(mFileEntity);
//					}
					// 2012-06-09 brucewang
					if(mStringEntity!=null && mStringEntity.length()>0){
						requestPut.setEntity( new StringEntity(mStringEntity) );
						mStringEntity = null;
					}

					for (NameValuePair h : headers) {
						requestPut.addHeader(h.getName(), h.getValue());
					}
					
					
					httpResponse = mHttpClient.execute(requestPut);
					break;

				 case HTTP_DEL:
					HttpDelete requestDel = new HttpDelete(strUrl);
					requestDel.setHeader("Locale", strLocale);
					httpResponse = mHttpClient.execute(requestDel);
					
					break;

				default :
					Logger.e(TAG, "Unsupported HTTP method");
					params.clear();
					return -1;
			}
			
			

			responseBody = getStringFromHttpResponse(httpResponse);

			params.clear();
			headers.clear();
			mFileEntity = null;
			responseCode = httpResponse.getStatusLine().getStatusCode();
			responseMessage = httpResponse.getStatusLine().getReasonPhrase();
			
			
			

			mCookies = ((DefaultHttpClient) mHttpClient).getCookieStore()
					.getCookies();
			if (!mCookies.isEmpty()) {
				for (int i = 0; i < mCookies.size(); i++) {
					// cookie = cookies.get(i);
					String cookieString = mCookies.get(i).getName() + "="
							+ mCookies.get(i).getValue();
					cookieManager.setCookie(url.getHost(), cookieString);
				}
			}
			
			
			Logger.e(TAG, String.format("Request for '%s' is finished", strUrl));
		} catch (Exception e) {
			Logger.e(TAG, "Exception while calling URL - " + strUrl);
			e.printStackTrace();
		}

		return responseCode;
	}

	private String lineEnd = "\r\n";
	private String twoHyphens = "--";
	private String boundary = "*****";
	
	public int HttpFileUpload(String urlString, String filedName, String fileLocation){
		return HttpFileUpload(urlString, filedName, fileLocation, "POST");
	}

	public int HttpFileUpload(String urlString, String filedName, String fileLocation, String httpRequestMethod) {

		int responseCode = 0;
		String result = null;

		// open connection
		HttpURLConnection conn = null;
		try {

			
			URL connectUrl = new URL(urlString);
			

			// open connection
			conn = (HttpURLConnection) connectUrl
					.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod(httpRequestMethod);//"POST");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Content-Type","multipart/form-data;boundary=" + boundary);
			conn.setConnectTimeout(mTimeout);
			conn.setReadTimeout(mTimeout);
			
			
			if( mUserId!=null && mUserId.length()>0 ){
				conn.setRequestProperty("user_id", mUserId);
			}
//			if( mDeviceId!=null && mDeviceId.length()>0 ){
//				conn.setRequestProperty("device_id", mDeviceId);
//			}
			
			
			// 2012-11-06 brucewang
			// send locale, useragent...
			Locale locale = mCtx.getResources().getConfiguration().locale;
			String strLocale = locale.toString();
			conn.setRequestProperty("Locale", strLocale);
			String userAgent = System.getProperty( "http.agent" );
			conn.setRequestProperty("User-Agent", userAgent);
			
			conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			conn.setRequestProperty("Accept-Charset", "windows-949,utf-8;q=0.7,*;q=0.3");
			conn.setRequestProperty("Accept-Encoding", "gzip,deflate,sdch");
			conn.setRequestProperty("Accept-Language", "ko-KR,ko;q=0.8,en-US;q=0.6,en;q=0.4");
			
			
			Logger.e(TAG, "Header - " + headers.toString());
			Logger.e(TAG, "Params - " + params.toString());
			Logger.e(TAG, "URL - " + urlString);

			

			// write data
			DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

			if (!params.isEmpty()) {
				for (int i = 0; i < params.size(); i++) {
					dos.writeBytes(twoHyphens + boundary + lineEnd);
					dos.writeBytes("Content-Disposition: form-data; name=\""
							+ params.get(i).getName() + "\"" + lineEnd);
					dos.writeBytes(lineEnd);

					
					// 2012-05-22 brucewang
					// Encoding 처리.
					//dos.writeBytes(URLEncoder.encode(params.get(i).getValue(), "UTF-8"));
					//dos.writeBytes( params.get(i).getValue().getBytes("UTF-8") );
					dos.write( params.get(i).getValue().getBytes("UTF-8") );
					//dos.writeUTF(params.get(i).getValue());

					dos.writeBytes(lineEnd);
				}
			}
			
			
			String filename = "";
			if( fileLocation!=null && fileLocation.length()>0 ){
				File file = new File(fileLocation);
				filename = file.getName();
			}

			
			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\""+filedName+"\"; filename=\""
					+ filename + "\"" + lineEnd);
			dos.writeBytes(lineEnd);

			if( fileLocation!=null && fileLocation.length()>0 ){
				FileInputStream mFileInputStream = new FileInputStream(fileLocation);
				
				int bytesAvailable = mFileInputStream.available();
				int maxBufferSize = 1024;
				int bufferSize = Math.min(bytesAvailable, maxBufferSize);
	
				byte[] buffer = new byte[bufferSize];
				int bytesRead = mFileInputStream.read(buffer, 0, bufferSize);
	
				// read image
				while (bytesRead > 0) {
					dos.write(buffer, 0, bufferSize);
					bytesAvailable = mFileInputStream.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					bytesRead = mFileInputStream.read(buffer, 0, bufferSize);
				}
				
				mFileInputStream.close();
			}

			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
			dos.flush(); // finish upload...

			
			// response
			BufferedReader bf = new BufferedReader(new InputStreamReader(
					conn.getInputStream(), "UTF-8"));
			StringBuilder buff = new StringBuilder();
			String line;
			while ((line = bf.readLine()) != null) {
				buff.append(line);
			}
			responseBody = buff.toString();
			
			// get response
			responseCode = conn.getResponseCode();
			Logger.e(TAG, "reponse " + responseCode);

			
			
			
			params.clear();
			headers.clear();

			
			result = buff.toString();

			
			Logger.e(TAG, "File is written");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(conn!=null){
			conn.disconnect();
		}
		

		return responseCode;
	}
}
