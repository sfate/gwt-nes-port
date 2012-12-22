package com.gochromium.nes.client.service;

import java.util.ArrayList;
import java.util.List;

import com.gochromium.nes.client.model.Genre;
import com.gochromium.nes.client.model.MetaData;
import com.google.gwt.regexp.shared.RegExp;

public class MetaDataServiceImpl implements MetaDataService {

	static List<MetaData> META_DATA_LIST = new ArrayList<MetaData>();

	static {
		// NUMBER ////////////////////////////////////////////////////////
		add("1942", ".*1942.*","1942.jpg");
		
		// B /////////////////////////////////////////////////////////////
		add("Baseball", ".*baseball.*","Baseball.jpg", Genre.SPORTS);
		add("Base Wars", ".*base.*wars.*","Base Wars.jpg", Genre.SPORTS);
		add("Batman", ".*batman.*","Batman.jpg", Genre.ACTION);
		add("Battletoads", ".*battle.*toads.*","Battletoads.jpg", Genre.ACTION);
		add("Bayou Billy, the Adventures of", ".*bayou.*billy.*","The Adventures of Bayou Billy.jpg", Genre.ADVENTURE);
		add("Bomberman", ".*bomber.*man.*","Bomberman.jpg", Genre.ACTION);
		add("Bubble Bobble", ".*bubble.*bobble.*","Bubble Bobble.jpg", Genre.PLATFORM);
		// C /////////////////////////////////////////////////////////////
		add("Castlevania III Dracula's Curse",".*castlevania.*(3|III).*","Castlevania3.jpg", Genre.PLATFORM);
		add("Castlevania II Simon's Quest", ".*castlevania.*(2|II).*","Castlevania2.jpg", Genre.PLATFORM);
		add("Castlevania", "^(?!.*2|.*3|.*ii).*castlevania.*$","Castlevania.jpg", Genre.PLATFORM);
		add("Cobra Triangle", ".*cobra.*triangle.*","Cobra Triangle.jpg", Genre.RACING);
		add("Contra", "^(?!.*super).*contra.*","Contra.jpg", Genre.ACTION);
		add("Commando",".*commando.*","Commando.jpg", Genre.ACTION);
		add("Chip'N Dale Resuce Ranges",".*chip.*dale.*","Chip 'N Dale Rescue Rangers.jpg",Genre.PLATFORM);
		// D /////////////////////////////////////////////////////////////
		add("Donkey Kong",".*donkey.*kong.*","DonkeyKongClassics.jpg",Genre.PLATFORM);
		add("Double Dragon III The Sacred Stones",".*double.*dragon.*(3|III).*","Double Dragon III The Sacred Stones.jpg", Genre.FIGHTING);
		add("Double Dragon II The Revenge", ".*double.*dragon.*(2|II).*","Double Dragon II The Revenge.jpg", Genre.FIGHTING);
		add("Double Dragon", "^(?!.*2|.*3|.*ii).*double.*dragon.*","Double Dragon.jpg",Genre.FIGHTING);
		add("Double Dribble", ".*double.*dribble.*","Double Dribble.jpg", Genre.SPORTS);
		add("Dragon Warrior III", ".*dragon.*warrior.*(3|III).*","Dragon Warrior III.jpg", Genre.ADVENTURE);
		add("Dragon Warrior", ".*dragon.*warrior.*","Dragon Warrior.jpg", Genre.ADVENTURE);
		add("Duck Tales", "^(?!.*2|.*ii).*duck.*tales.*","DuckTales.jpg", Genre.PLATFORM);
		// E /////////////////////////////////////////////////////////////
		add("Excite Bike",".*excite.*bike.*","ExciteBike.jpg", Genre.RACING);
		// F /////////////////////////////////////////////////////////////
		add("Final Fantasy", ".*final.*fantasy.*","FinalFantasy.jpg");
		add("Friday the 13th", ".*friday.*13.*","FridayThe13th.jpg",Genre.PLATFORM);
		// G /////////////////////////////////////////////////////////////
		add("Galaga Demons of Death", ".*galaga.*","Galaga Demons of Death.jpg", Genre.ACTION);
		add("Ghostbusters II", ".*ghostbuster.*(2|II).*","Ghostbusters2.jpg", Genre.ACTION);
		add("Ghostbusters", "^(?!.*2|.*ii).*ghostbuster.*","Ghostbusters.jpg",Genre.ACTION);
		
		add("Golf", "^golf.*","Golf.jpg", Genre.SPORTS);
		add("Goonies 2", ".*goonies.*","Goonies2.jpg", Genre.PLATFORM);
		add("Gradius", ".*gradius.*","Gradius.jpg", Genre.ACTION);
		// H /////////////////////////////////////////////////////////////
		add("Hook", ".*hook.*","Hook.jpg", Genre.ACTION);
		// I /////////////////////////////////////////////////////////////
		add("Ice Climber", ".*ice.*climber.*","Ice Climber.jpg", Genre.ACTION);
		add("Ice Hockey", ".*ice.*hockey.*","Ice Hockey.jpg", Genre.SPORTS);
		// K /////////////////////////////////////////////////////////////
		add("Kid Icarus",".*kid.*icarus.*","KidIcarus.jpg", Genre.PLATFORM);
		add("Kirby's Adventure",".*kirby.*","KirbysAdventure.jpg",Genre.PLATFORM);
		add("Kung Fu",".*kung.*fu.*","Kung Fu.jpg",Genre.FIGHTING);
		// L /////////////////////////////////////////////////////////////
		add("Lee Trevino's Fighting Golf", ".*trevino.*golf.*","LeeTrevinoFightingGolf.jpg", Genre.SPORTS);
		add("Legendary Wings",".*legend.*wing.*","Legendary Wings.jpg",Genre.PLATFORM);
		// M /////////////////////////////////////////////////////////////
		add("Metroid",".*metroid.*","Metroid.jpg", Genre.ADVENTURE);
		add("Mega Man 2", ".*mega.*man.*(2|II).*","Mega Man 2.jpg",Genre.ACTION);
		add("Mega Man 3", ".*mega.*man.*(3|III).*","Mega Man 3.jpg",Genre.ACTION);
		add("Mega Man 4", ".*mega.*man.*(4|IV).*","Mega Man 4.jpg",Genre.ACTION);
		add("Mega Man 5", ".*mega.*man.*(5|V).*","Mega Man 5.jpg",Genre.ACTION);
		add("Mega Man 6", ".*mega.*man.*(6|VI).*","Mega Man 6.jpg",Genre.ACTION);
		add("Mega Man", "^(?!.*2|.*3|.*4|.*5|.*6|.*i).*mega.*man.*","Mega Man.jpg",Genre.ACTION);
		//Mario Games
		add("Super Mario Bros 3",".*mario.*(3|III).*","SuperMarioBros3.jpg",Genre.PLATFORM);
		add("Super Mario Bros 2", ".*mario.*(2|II).*","SuperMarioBros2.jpg",Genre.PLATFORM);
		add("Super Mario Bros", "^(?!.*2|.*3|.*dr|.*do|.*ii).*sup.*mario.*$","SuperMarioBros.jpg",Genre.PLATFORM);
		add("Dr. Mario",".*(dr|doctor).*mario.*","DrMario.jpg",Genre.PUZZLE);
		// N /////////////////////////////////////////////////////////////
		add("Ninja Gaiden",".*ninja.*gaiden.*","Ninja Gaiden.jpg",Genre.PLATFORM);
		add("Nintendo World Cup",".*nintendo.*world.*cup.*","Nintendo World Cup.jpg",Genre.SPORTS);
		// O /////////////////////////////////////////////////////////////
		add("Open Tournament Golf",".*open.*tourn.*golf.*","NES Open Tournament Golf.jpg",Genre.SPORTS);
		// P /////////////////////////////////////////////////////////////
		add("Paperboy",".*paperboy.*","Paperboy.jpg");
		add("Pinball",".*pinball.*","Pinball.jpg");
		add("Punch Out",".*punch.*out.*","PunchOut.jpg",Genre.SPORTS);
		add("Pro Wrestling",".*pro.*wrest.*","Pro Wrestling.jpg",Genre.SPORTS);
		// R /////////////////////////////////////////////////////////////
		add("R.C. Pro-Am II", ".*r.c.*pro.*am.*(ii|2).*","R.C.ProAm2.jpg",Genre.RACING);
		add("R.C. Pro-Am", "^(?!.*2|.*ii).*r.c.*pro.*am.*","R.C.jpg",Genre.RACING);
		add("Rad Racer",".*rad.*racer.*","RadRacer.jpg",Genre.RACING);
		add("Robo Cop",".*robo.*cop.*","RoboCop.jpg",Genre.ACTION);
		add("Rygar",".*rygar.*","Rygar.jpg",Genre.ACTION);

		// S /////////////////////////////////////////////////////////////
		add("Section-Z","^section.*Z.*","Section-Z.jpg",Genre.ACTION);
		add("Shadowgate","^shadowgate.*","Shadowgate.jpg",Genre.ADVENTURE);
		add("Soccer","^soccer.*","Soccer.jpg",Genre.SPORTS);
		add("Skate or Die 2",".*skate.*die.*(ii|2).*","Skate or Die 2.png",Genre.SPORTS);
		add("Skate or Die","^(?!.*2|.*ii).skate.*die.*","Skate Or Die.jpg",Genre.SPORTS);
		add("Solar Jetman",".*solar.*jetman.*","Solar Jetman.jpg");
		add("Spy Hunter",".*spy.*hunter.*","Spy Hunter.jpg",Genre.RACING);
		add("Spy vs Spy",".*spy.*spy.*","Spy vs.jpg");
		add("Star Tropics II, Zoda's Revenge", ".*star.*tropics.*(2|II).*","StarTropics2.jpg",Genre.ADVENTURE);
		add("Star Tropics", "^(?!.*2|.*ii).*star.*tropics.*","StarTropics.jpg",Genre.ADVENTURE);
		add("Super C", ".*super.*c.*","SuperContra.jpg",Genre.ACTION);

		// T /////////////////////////////////////////////////////////////
		add("T & C Surf Designs",".*surf.*designs.*","T & C Surf Designs Wood & Water Rage.jpg",Genre.SPORTS);
		add("Tecmo Bowl","^(?!.*super).*tecmo.*bowl.*","Tecmo Bowl.jpg",Genre.SPORTS);
		add("Tecmo Super Bowl",".*tecmo.*super.*bowl.*","Tecmo Super Bowl.jpg",Genre.SPORTS);
		add("Tennis", "^(?!.*2|.*ii).*tennis.*","Tennis.jpg", Genre.PUZZLE);
		add("Tetris 2", ".*tetris.*(ii|2).*","Tetris2.jpg", Genre.PUZZLE);
		add("Tiny Toon Adventures", ".*tiny.*toon.*","TinyToonAdventures.jpg",Genre.ADVENTURE);
		add("Top Gun", ".*top.*gun.*","TopGun.jpg",Genre.ACTION);
		add("The Simpsons, Bart vs the World", ".*bart.*world.*","The Simpsons Bart vs.jpg",Genre.ACTION);
		add("The Simpsons, Bart vs the Space Mutants", ".*bart.*mutants.*","The Simpsons Bart vs the Space Mutants.jpg",Genre.ACTION);
		add("Tenage Mutant Ninja Turtles III",".*teenage.*mutant.*(3|III).*","TMNT3.jpg",Genre.ACTION);
		add("Tenage Mutant Ninja Turtles II", ".*teenage.*mutant.*(2|II).*","TMNT2.jpg",Genre.ACTION);
		add("Tenage Mutant Ninja Turtles", "^(?!.*2|.*3|.*ii).*teenage.*mutant.*$","TMNT.jpg",Genre.ACTION);
		
		
		
		// V /////////////////////////////////////////////////////////////
		add("Volleyball",".*volleyball.*","Volleyball.jpg", Genre.SPORTS);
		// W /////////////////////////////////////////////////////////////
		add("Willow",".*willow.*","Willow.jpg", Genre.ADVENTURE);
		// Z /////////////////////////////////////////////////////////////
		add("Zelda II, The Adventure Of Link", ".*zelda.*(2|II).*","Zelda2.jpg",Genre.ADVENTURE);
		add("Zelda, The Legend Of", "^(?!.*2|.*ii).*zelda.*$","Zelda.jpg",Genre.ADVENTURE);
		
		
		
	}
	
	static void add(String name, String pattern, String image) {
		META_DATA_LIST.add(new MetaData(
				name, pattern, "gwt_nes_port/"+image));
	}
	
	static void add(String name, String pattern, String image, String tags) {
		META_DATA_LIST.add(new MetaData(
				name, pattern, "gwt_nes_port/"+image, tags));
	}

	@Override
	public MetaData findMetaData(String file) {
		String fileName = file.toLowerCase();
		
		for(MetaData metaData : META_DATA_LIST) {

			if(RegExp.compile(metaData.getPattern(),"i").test(fileName)) {
				return metaData;//new Game(fileName,metaData.getName(),metaData.getImage());
			}
		}
		
		return null;
	}
}
