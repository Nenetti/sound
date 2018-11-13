package dictionary;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import process.Terminal;

public class Converter {

	public static void main(String[] args) throws Exception{
		if(args.length==4) {
			BufferedReader reader=new BufferedReader(new FileReader(new File(args[0])));
			BufferedWriter writer_en=new BufferedWriter(new FileWriter(new File(args[1])));
			BufferedWriter writer_jp=new BufferedWriter(new FileWriter(new File(args[2])));
			String line;
			while((line=reader.readLine())!=null) {
				String[] cells=line.split(",");
				String english;
				String japanese;
				switch (cells.length) {
				case 3:
					english=cells[0].replaceAll(" ", "_");
					writer_en.write(english+"\t"+cells[1]);
					writer_en.newLine();
					break;
				case 6:
					english=cells[0].replaceAll(" ", "_");
					writer_en.write(english+"\t"+cells[1]);
					writer_en.newLine();
					japanese=cells[3].replaceAll(" ", "_");
					writer_jp.write(japanese+"\t"+cells[4]);
					writer_jp.newLine();
					break;
				}
			}
			reader.close();
			writer_en.close();
			writer_jp.close();
			
			String en=new File(args[1]).getName();
			String jp=new File(args[2]).getName();
			Terminal.commands("bash", args[3], en.substring(0, en.lastIndexOf("."))).execute();
			Terminal.commands("bash", args[3], jp.substring(0, jp.lastIndexOf("."))).execute();
		}else {
			System.out.println(args.length);
			System.out.println("引数の数が違います");
			System.out.println("<対象のファイル> <英語読み仮名ファイル> <日本語読み仮名ファイル>");
		}
	}
	
}