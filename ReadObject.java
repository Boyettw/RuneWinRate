package brawlers;
import java.util.*;
import java.io.*;
import com.google.gson.*;
import com.robrua.orianna.api.core.RiotAPI;
import com.robrua.orianna.api.core.StaticDataAPI;
import com.robrua.orianna.api.dto.MatchAPI;
import com.robrua.orianna.store.*;
import com.robrua.orianna.type.core.common.QueueType;
import com.robrua.orianna.type.core.common.Region;
import com.robrua.orianna.type.core.common.Side;
import com.robrua.orianna.type.core.currentgame.RuneCount;
import com.robrua.orianna.type.core.league.League;
import com.robrua.orianna.type.core.staticdata.Champion;
import com.robrua.orianna.type.core.summoner.Summoner;
import com.robrua.orianna.type.dto.match.MatchDetail;
import com.robrua.orianna.type.dto.staticdata.Info;
import com.robrua.orianna.type.dto.staticdata.Rune;
import com.robrua.orianna.type.dto.staticdata.RuneList;
import com.robrua.orianna.type.core.match.*;
import com.robrua.orianna.type.core.matchlist.MatchReference;
import com.robrua.orianna.type.api.RateLimit;
import com.robrua.orianna.type.core.OriannaObject;
public class ReadObject {
	public static void main(String args[]){
		HashMap<String, HashMap<String, TreeMap<Double, String>>> result = null;
		   try{
		      InputStream file = new FileInputStream("C:/eclipse/brawlers/src/resultObj1.txt");
		      InputStream buffer = new BufferedInputStream(file);
		      ObjectInput input = new ObjectInputStream (buffer);
		      result = (HashMap<String, HashMap<String, TreeMap<Double, String>>>)input.readObject();
		      input.close();
		   }catch(Exception e){
			  System.out.println("ow, file not found.");
		      System.exit(1);;
		   }
		   for(Double pcnt : result.get("Swain").get("AD").keySet()){
			   System.out.println("RuneID " + result.get("Swain").get("AD").get(pcnt)+ "  " + (pcnt*100) + "%");
		   }
		      
		   }
}
