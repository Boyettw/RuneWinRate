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


public class brawlers {
	static long startTime = System.currentTimeMillis();
	static HashMap<Long, HashMap<String, HashMap<Long, Double[]>>> champions = new HashMap<Long, HashMap<String, HashMap<Long, Double[]>>>();
	static HashMap<String, HashMap<String, TreeMap<Double, String>>> result = new HashMap<String, HashMap<String, TreeMap<Double, String>>>();
	static Cache cache = new Cache();
	static String filePath;
	
	//---------------------------------------------------------------------------------------
    public static void main(String args[]) {   
    	  Setup();
        Creation();
        ThruFile(ReadFile());
        Results();
        ToFile();
        RunTime();
    }
    //---------------------------------------------------------------------------------------
    public static Long[] ReadFile(){
    	try{
        	BufferedReader br = new BufferedReader(new FileReader(filePath));
        	return new Gson().fromJson(br, Long[].class);
        
    	} catch ( java.io.FileNotFoundException e ) {
    		System.out.println("ouch, file not found");
    		System.exit(1);
    	}
    	return null;
    }
    //---------------------------------------------------------------------------------------
    public static void Creation(){
    	Long hashString;
    	String resultHashString;
    	for (Champion champ: StaticDataAPI.getChampions()){
    		hashString = champ.getID();
    		resultHashString = StaticDataAPI.getChampionByID(hashString).getName();
    		result.put(resultHashString, (HashMap<String, TreeMap<Double, String>>) new HashMap());
    		champions.put(hashString , (HashMap<String, HashMap<Long, Double[]>>) new HashMap());
    		champions.get(hashString).put("AP", (HashMap<Long, Double[]>) new HashMap());
    		champions.get(hashString).put("AD", (HashMap<Long, Double[]>) new HashMap());
    		champions.get(hashString).put("Neutral", (HashMap<Long, Double[]>) new HashMap());
    		result.get(resultHashString).put("AP", (TreeMap<Double, String>) new TreeMap());
    		result.get(resultHashString).put("AD", (TreeMap<Double, String>) new TreeMap());
    		result.get(resultHashString).put("Neutral", (TreeMap<Double, String>) new TreeMap());
    		Double[] initial = {0.0, 0.0, 0.0};
    		for (String teamComp: champions.get(hashString).keySet()){
    			for(com.robrua.orianna.type.core.staticdata.Rune rune : StaticDataAPI.getRunes()){
    				champions.get(hashString).get(teamComp).put(new Long(rune.getID()), initial.clone());
    			}
    		}	
    	}                                          //this string points at one of the 3 hashmaps
     
    }
    //---------------------------------------------------------------------------------------
    public static void ThruFile(Long[] NAgames){
    	int count = 0;
        for (long gameId : NAgames){
		        	try{
		        		Insertion(gameId);
		        		count += 1;
		        		System.out.println(count + "/10000 " + " inserted " + gameId);

		        	} catch(Exception exc){
		        		System.out.println("Failed to insert " + gameId + " because:");
		        		exc.printStackTrace();
		        	}
        }
    }
    //---------------------------------------------------------------------------------------
    public static void Insertion(Long gameId){ 
    	Match game = RiotAPI.getMatch(gameId);
    	HashSet<Participant> blue = new HashSet<Participant>();
    	HashSet<Participant> purple = new HashSet<Participant>();
    	//add each participant to a "team", get each participants runes, calculate the stats of the opposing team,
    	for(Participant player : game.getParticipants()){
    		if (player.getTeam() == Side.BLUE){
    			blue.add(player);
    		}else{
    			purple.add(player);
    		}
    	}
    	//call function that will insert runes to the winning team and losing team appropriately  (the strings for the comps of the two teams)
    	InsertRunes(game, DetermineTeam(blue), DetermineTeam(purple), blue, purple);
    	cache.delete(Match.class , gameId.longValue());
    }
    //---------------------------------------------------------------------------------------
    public static String DetermineTeam(HashSet<Participant> players){
    	double magic = 0;
    	double attack = 0; 
//    	double defense = 0;
    	com.robrua.orianna.type.core.staticdata.Info current;
    	
    	for(Participant player : players){//we need to put way more time into this function!!
    		current = StaticDataAPI.getChampionByID(player.getChampionID()).getInfo();
    		magic += current.getMagic();
    		attack += current.getAttack();
//    		defense  += current.getDefense();
    	}
    	//put in section for more team calculations
    	if(magic > 1.4 * attack){
    		return "AP";
    	}else if(attack > 1.4 * magic){
    		return "AD";
    	}else {
    		return "Neutral";
    	}
    }
    //---------------------------------------------------------------------------------------
    public static void InsertRunes(Match game, String blueTeamComp, String purpleTeamComp, HashSet<Participant> blue, HashSet<Participant> purple){
    	List<com.robrua.orianna.type.core.staticdata.Rune> runes = StaticDataAPI.getRunes();
    	MatchTeam tempTeam;
    	Double[] tempDouble = null;
    	tempTeam = game.getTeams().get(0);// determine if blue or purp and if they won or not
    	if(tempTeam.getSide() == Side.BLUE){
    		if(tempTeam.getWinner()){//put in doubles for win with runeset
    			for(Participant player : blue){
    				if(player.getDto().getParticipant().getRunes() != null){
	    				for(com.robrua.orianna.type.dto.match.Rune rune : player.getDto().getParticipant().getRunes()){
	    					if(runes.contains(StaticDataAPI.getRune(rune.getRuneId()))){
		    					tempDouble = champions.get(player.getChampionID()).get(purpleTeamComp).get(rune.getRuneId());
		    					tempDouble[0] += rune.getRank();
		    					tempDouble[1] += rune.getRank();
		    					tempDouble[2] = tempDouble[0] / tempDouble[1];
	    					}
	    				}	
	    			}
    			}	
    			for(Participant player : purple){
        			if(player.getDto().getParticipant().getRunes() != null){
    	    			for(com.robrua.orianna.type.dto.match.Rune rune : player.getDto().getParticipant().getRunes()){
    	    				if(runes.contains(StaticDataAPI.getRune(rune.getRuneId()))){
	    	    				tempDouble = champions.get(player.getChampionID()).get(blueTeamComp).get(rune.getRuneId());
	    	    				tempDouble[1] += rune.getRank();
	    	    				if(tempDouble[0] > 0){
	    	    					tempDouble[2] = tempDouble[0] / tempDouble[1];
	    	    				}
    	    				}
    	    			}	
    				}
    			}
    			
    		}else{
    			for(Participant player : blue){
    				if(player.getDto().getParticipant().getRunes() != null){
	    				for(com.robrua.orianna.type.dto.match.Rune rune : player.getDto().getParticipant().getRunes()){
	    					if(runes.contains(StaticDataAPI.getRune(rune.getRuneId()))){
		    					tempDouble = champions.get(player.getChampionID()).get(purpleTeamComp).get(rune.getRuneId());
		    					tempDouble[1] += rune.getRank();
		    					if(tempDouble[0] > 0){
		    						tempDouble[2] = tempDouble[0] / tempDouble[1];
		    					}
	    					}	
	    				}
    				}
    			}
    			for(Participant player : purple){
    				if(player.getDto().getParticipant().getRunes() != null){
	    				for(com.robrua.orianna.type.dto.match.Rune rune : player.getDto().getParticipant().getRunes()){
	    					if(runes.contains(StaticDataAPI.getRune(rune.getRuneId()))){
	    						tempDouble = champions.get(player.getChampionID()).get(blueTeamComp).get(new Long(rune.getRuneId()));
	    						tempDouble[0] += rune.getRank();
	    						tempDouble[1] += rune.getRank();
	    						tempDouble[2] = tempDouble[0] / tempDouble[1];
	    					}	
	    				}
    				}
    			}
    		}
    		
    	}else{
    		if(tempTeam.getWinner()){//if purple won
    			for(Participant player : purple){
    				if(player.getDto().getParticipant().getRunes() != null){
	    				for(com.robrua.orianna.type.dto.match.Rune rune : player.getDto().getParticipant().getRunes()){
	    					if(runes.contains(StaticDataAPI.getRune(rune.getRuneId()))){
		    					tempDouble = champions.get(player.getChampionID()).get(blueTeamComp).get(rune.getRuneId());
		    					tempDouble[0] += rune.getRank();
		    					tempDouble[1] += rune.getRank();
		    					tempDouble[2] = tempDouble[0] / tempDouble[1];
	    					}
	    				}
    				}
    			}
    			for(Participant player : blue){
    				if(player.getDto().getParticipant().getRunes() != null){
	    				for(com.robrua.orianna.type.dto.match.Rune rune : player.getDto().getParticipant().getRunes()){
	    					if(runes.contains(StaticDataAPI.getRune(rune.getRuneId()))){
		    					tempDouble = champions.get(player.getChampionID()).get(purpleTeamComp).get(rune.getRuneId());
		    					tempDouble[1] += rune.getRank();
		    					if(tempDouble[0] > 0){
		    						tempDouble[2] = tempDouble[0] / tempDouble[1];
		    					}
	    					}
	    				}
    				}
    			}
    		}else{//put in doubles for loss with runeset
    			for(Participant player : purple){
    				if(player.getDto().getParticipant().getRunes() != null){
	    				for(com.robrua.orianna.type.dto.match.Rune rune : player.getDto().getParticipant().getRunes()){
	    					if(runes.contains(StaticDataAPI.getRune(rune.getRuneId()))){
		    					tempDouble = champions.get(player.getChampionID()).get(blueTeamComp).get(rune.getRuneId());
		    					tempDouble[1] += rune.getRank();
		    					tempDouble[2] = tempDouble[0] / tempDouble[1];
	    					}
	    				}	
    				}
    			}
    			for(Participant player : blue){
    				if(player.getDto().getParticipant().getRunes() != null){
	    				for(com.robrua.orianna.type.dto.match.Rune rune : player.getDto().getParticipant().getRunes()){
	    					if(runes.contains(StaticDataAPI.getRune(rune.getRuneId()))){
		    					tempDouble = champions.get(player.getChampionID()).get(purpleTeamComp).get(rune.getRuneId());
		    					tempDouble[0] += rune.getRank();
		    					tempDouble[1] += rune.getRank();
		    					tempDouble[2] = tempDouble[0] / tempDouble[1];
	    					}
	    				}	
    				}
    			}
    		}
    	}
    }
    //---------------------------------------------------------------------------------------
    public static void Results(){
    	String resultHashString;
    	//HashMap<Long, HashMap<String, HashMap<Double, Long>>>
    	for(Long ChampID : champions.keySet()){
    		resultHashString = StaticDataAPI.getChampionByID(ChampID).getName();
    		for(String teamComp : champions.get(ChampID).keySet()){
    			for(Long runeID : champions.get(ChampID).get(teamComp).keySet()){
    				if ((champions.get(ChampID).get(teamComp).get(runeID)[1] > 25) && (StaticDataAPI.getRune(runeID).getName().contains("Greater"))){
    					result.get(resultHashString).get(teamComp).put(champions.get(ChampID).get(teamComp).get(runeID)[2], StaticDataAPI.getRune(runeID).getName());
    				}
    			}
    		}
    	}
    }
    //---------------------------------------------------------------------------------------
    public static void ToFile(){
	    ObjectOutputStream oos = null;
	    FileOutputStream fout = null;
	    try{
	            fout = new FileOutputStream("resultObj1.txt", false);
	            oos = new ObjectOutputStream(fout);
	            oos.writeObject(result);
	    } catch (Exception ex) {
	    		ex.printStackTrace();
	            System.out.println("ow");
	    }
	    try{
	    	oos.close();
	    } catch (Exception exc){
	    	System.out.println("failed to print to file");
	    }
    }
    //---------------------------------------------------------------------------------------
    public static void RunTime(){
    	System.out.print((System.currentTimeMillis() - startTime) + " milliseconds");
    }
    //---------------------------------------------------------------------------------------
    public static void Setup(){
    	Scanner scanIn = new Scanner(System.in);
    	System.out.println("Enter API Key");
    	String APIKey = scanIn.nextLine();
    	System.out.println("Enter path/Jsonfile");
    	filePath = scanIn.nextLine();
    	System.out.println("Enter Region eg. NA || KR || EU...");
    	String region = scanIn.nextLine();
    	scanIn.close();
    	RiotAPI.setMirror(Region.valueOf(region));
        RiotAPI.setRegion(Region.valueOf(region));
        RiotAPI.setAPIKey(APIKey);
        RiotAPI.setDataStore(cache);
    }
    //---------------------------------------------------------------------------------------
}    

