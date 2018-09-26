
package recognition.module;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import process.Terminal;



public class Julius {

	private String storagePath="ros/sound";
	private String command="boot.sh";
	private String path="ros/sound/julius";
	private String host;
	private int port;

	private BufferedReader reader;

	private OutputStream stream;

	private Socket socket;
	
	public double score;

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
	public Julius(final String host, final int port) {
		this.host=host;
		this.port=port;
		setup();
	}

	/******************************************************************************************
	 * 
	 * 初期設定
	 */
	private void setup() {
		Terminal.execute(toShellCommand(path, command)+" "+toPath(path)+" "+port, false, true);
		socket_connect();
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
						System.out.println("Julius Connect SUCCESSFUL");
						reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
						stream=socket.getOutputStream();
						break;
					} catch (Exception e) {
						System.out.println("Connect Failed "+port);
						duration(1000);
					}
				}
			}
		}).start();
	}

	
	public void pause() {
		try {
			stream.write(("TERMINATE"+"\n").getBytes());
			stream.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void resume() {
		try {
			stream.write(("RESUME"+"\n").getBytes());
			stream.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void playWav(String fileName) {
		Terminal.execute("aplay "+toPath(storagePath, fileName+".wav"), true, false);
	}
	
	/******************************************************************************************
	 * 
	 * juliusに音声ファイルパスを投げて音声認識させる
	 */
	public String recognition() {
		try {
			return getResult();
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return null;
	}

	/******************************************************************************************
	 * 
	 * juliusから返ってくる認識結果の文字を取り出す
	 * 
	 * @return
	 */
	private String getResult() {
		String line;
		while(true) {
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
								this.score=Double.valueOf(split[7]);
								System.out.println(score+" : "+split[1].replace('^', ' '));
								String result=split[1].replace('^', ' ');
								if(score>0.9) {
									if(result.length()>3) {
										return result;
									}
									return null;
								}else {
									if(result.length()>3) {
										return "n";	
									}
									return null;
								}
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
			return socket.isConnected();
		}
		return false;
	}

	/******************************************************************************************
	 * 
	 * @param args
	 * @return
	 */
	private String toPath(String... args) {
		if(args!=null) {
			String path=System.getProperty("user.home");
			for(String arg: args) {
				path+="/"+arg;
			}
			return path;
		}
		return null;
	}
	
	/******************************************************************************************
	 * 
	 * @param args
	 * @return
	 */
	private String toShellCommand(String... args) {
		if(args!=null) {
			String path="bash "+System.getProperty("user.home");
			for(String arg: args) {
				path+="/"+arg;
			}
			return path;
		}
		return null;
	}

	/******************************************************************************************
	 * 
	 * @param time
	 */
	private void duration(int time) {
		try {
			Thread.sleep(time);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
