
package recognition.module;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import process.Terminal;
import ros.NodeHandle;



public class Julius {

	private String home=System.getProperty("user.home");
	private String path="ros/sound/julius";
	private String shell="boot.sh";
	private String host;
	private String exec=home+"/"+path+"/"+shell;
	private int port;

	private BufferedReader reader;

	private OutputStream stream;

	private Socket socket;
	
	public String result;

	enum type{
		SHYP_S,
		SHYP_E,
		WHYPO_WORD,
		REJECTED_REASON,
		START,
		END;
	}



	/******************************************************************************************
	 * 
	 * コンストラクター
	 * 
	 * @param host
	 * @param port
	 */
	public Julius(String jconf, String host, int port) {
		this.host=host;
		this.port=port;
		run("bash", exec, jconf, String.valueOf(port));
		socket_connect();
	}
	
	private void run(String... command) {
		Terminal.execute(command, false, false);
	}

	/******************************************************************************************
	 * 
	 * juliusにソケットで接続 (認識結果を受け取るため)
	 */
	private void socket_connect() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {
						socket=new Socket();
						InetSocketAddress address=new InetSocketAddress(host, port);
						socket.connect(address, 1000000000);
						System.out.println("Julius Connect SUCCESSFUL: "+port);
						reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
						stream=socket.getOutputStream();
						break;
					} catch (Exception e) {
						System.out.println("Connected Failed: "+port);
						NodeHandle.duration(100);
					}
				}
			}
		}).start();
	}
	
	/******************************************************************************************
	 * 
	 */
	public void pause() {
		try {
			System.out.println("PAUSE: "+port);
			stream.write(("TERMINATE"+"\n").getBytes());
			stream.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/******************************************************************************************
	 * 
	 */
	public void resume() {
		try {
			System.out.println("RESUME: "+port);
			stream.write(("RESUME"+"\n").getBytes());
			stream.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/******************************************************************************************
	 * 
	 * juliusに音声ファイルパスを投げて音声認識させる
	 */
	public Result recognition() {
		return getResult();
	}
	
	/******************************************************************************************
	 * 
	 * juliusから返ってくる認識結果の文字を取り出す
	 * 
	 * @return
	 */
	private Result getResult() {
		String line;
		while(!socket.isClosed()&&socket.isConnected()) {
			try {
				line=reader.readLine();
				if(line!=null) {
					line=line.trim();
					if(line.length()>4) {
						type type=getLineType(line);
						if(type!=null) {
							switch (type) {
							case WHYPO_WORD:
								String[] split=line.split("\"");
								double score=Double.valueOf(split[7]);
								String result=split[1].replaceAll("_", " ");
								System.out.println(score+" : "+result);
								return new Result(result, score);
							case REJECTED_REASON:
								return null;
							case END:
								return null;
							case SHYP_S:
								break;
							default:
								break;
							}
						}
					}
				}
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}

	/******************************************************************************************
	 * 
	 * juliusから帰ってくる結果内容を分類する
	 * 
	 * @param line
	 * @return
	 */
	private type getLineType(String line) {
		switch (line.substring(1, 4)) {
		case "REC":
			// RECOGOU
			// 判定内容に問題がないときの内容表示開始フラグ
			break;
		case "/RE":
			// /RECOGOU
			// 判定内容に問題がないときの内容表示終了フラグ
			break;
		case "SHY":
			// SHYP
			// 認識内容に対する評価など
			return type.SHYP_S;
		case "/SH":
			// /SHYP
			// 認識内容に対する評価など含めの終了
			return type.SHYP_E;
		case "WHY":
			// WHYPO WORD
			//実際に内容を表示する部分
			return type.WHYPO_WORD;
		case "INP":
			// INPUT STATUS
			// INPUTPARAM FRAMES
			break;
		case "STA":
			// STARTRECOG

			break;
		case "END":
			// ENDRECOG
			break;
		case "REJ":
			// REJECTED REASON
			return type.REJECTED_REASON;
		}
		return null;
	}

	/******************************************************************************************
	 * 
	 * juliusに接続されているかを返す
	 * 
	 * @return
	 */
	public boolean isConnected() {
		if(socket!=null) {
			return !socket.isClosed()&&socket.isConnected();
		}
		return false;
	}
	
	public class Result {
		public String sentence;
		public double score;
		public Result(String sentence, double score) {
			this.sentence=sentence;
			this.score=score;
		}
	}

}
