import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class SocketChat3 extends JFrame implements Runnable {

	//GUI関係の変数
	JPanel pane;
	Container cont;
	JMenuBar menuBar;
	JMenu menu;
	JTextField tf;
	JTextArea resArea;
	JScrollPane area;
	JButton send;
	JMenuItem menuConnect;
	JMenuItem menuDisconnect;

	//通信関係の変数
	String connectHost;
	int connectPort;
	int myPort;
	ServerSocket serverSocket;
	Socket socket = null;
	PrintWriter pw;
	BufferedReader br;

	public static void main(String[] args){
		SocketChat3 schat = new SocketChat3("ScocketChat3", args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
		schat.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		schat.setSize(350,300);
		schat.setVisible(true);
		schat.startServer();
	}

	public SocketChat3(String title, String host, int port, int myPort){
		super(title);
		initGUI();
		//通信相手の初期化
		//相手のホスト名
		this.connectHost = host;
		//相手のポート番号
		this.connectPort = port;
		//自分のポート番号
		this.myPort = myPort;
	}

	public void initGUI(){
		//パネルの作成とコンテントペインへの貼り付け
		pane = new JPanel();

		//メニュー
		menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);

		menu = new JMenu("メニュー");
		menuBar.add(menu);

		//接続メニュー
		menuConnect = new JMenuItem("接続");
		menu.add(menuConnect);
		menuConnect.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				connectActionPerformed(e);
			}
		});

		//切断メニュー
		menuDisconnect = new JMenuItem("切断");
		menu.add(menuDisconnect);
		menuDisconnect.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				disconnectActionPerformed(e);
			}
		});

		
		pane.setLayout(new BorderLayout());

		//入力用テキストエリア
		tf = new JTextField();
		pane.add(tf,BorderLayout.NORTH);

		//スクロールバー付き応答履歴エリア
		resArea = new JTextArea();
		area = new JScrollPane();
		pane.add(resArea,BorderLayout.CENTER);
		resArea.add(area);

		//送信ボタン
		send = new JButton("送信");
		pane.add(send,BorderLayout.SOUTH);
		send.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				sendActionPerformed(e);
			}
		});

		Container c = getContentPane();
	    c.add(pane);

	}

	void startServer(){
		try{
			//サーバソケットの作成
			serverSocket = new ServerSocket(connectPort);
			socket = serverSocket.accept();
			resArea.append(connectHost+"に接続しました\n");
			OutputStream out = socket.getOutputStream();
			pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out)));
			InputStream in = socket.getInputStream();
			br = new BufferedReader(new InputStreamReader(in));
			//スレッドのスタート
			Thread th = new Thread(this);
			th.start();
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	//スレッドの仕事内容
	public void run(){
		try{
			String input;
			//受信データを行単位で書きだす
			while((input=br.readLine())!=null){
				synchronized(resArea){
					resArea.append("相手："+input+"\n");
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}

		//通信が終了した場合
		resArea.append(socket.getInetAddress().getHostName()+"との接続を切断しました\n");
	}

	//「送信」ボタンが押されたとき
	void sendActionPerformed(ActionEvent e){
		synchronized(resArea){
			//入力データを取得
			String message = this.tf.getText();
			try{
					pw.println(message);
					pw.flush();
			}catch(Exception ex){
				ex.printStackTrace();
			}

			//送信したデータを自分の画面にも表示
			resArea.append("自分："+message+"\n");
			tf.setText(null);
		}
	}

	//接続メニューが選択されたとき
	void connectActionPerformed(ActionEvent e){
		//接続要求
		try{
			//クライアントソケットの作成
			socket = new Socket(connectHost,myPort);
			OutputStream out = socket.getOutputStream();
			pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out)));
			InputStream in = socket.getInputStream();
			br = new BufferedReader(new InputStreamReader(in));

			this.resArea.append(connectHost+"："+connectPort+"に接続しました\n");

			Thread th = new Thread(this);
			th.start();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	void disconnectActionPerformed(ActionEvent e){
		//切断要求
		try{
			pw.close();
			br.close();
			socket.close();
		}catch(Exception ex2){
			ex2.printStackTrace();
		}
	}
}
