package io.gab.challenge.logreader.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.BasicJsonParser;
import org.springframework.boot.json.JsonParseException;
import org.springframework.stereotype.Controller;

import io.gab.challenge.logreader.data.Event;
import io.gab.challenge.logreader.repository.EventRepository;

@Controller
public class EventController {
	
	@Autowired
    private EventRepository eventRepository;
	
	public void processLogFile(String logFile) {
		if(logFile != null && !logFile.equals("")) {
			if(existsLogFile(logFile)) {
				
				// We are using a buffer reader to not load all the content of the file in memory
				// this way we are going through all the file line by line
				try(BufferedReader logFileBuffer = Files.newBufferedReader(Paths.get(logFile))) {
					Map<String, Event> events = new HashMap<String, Event>(); 
					
					String logLine;
					while((logLine = logFileBuffer.readLine()) != null) {
						processLogLine(logLine, events);
					}
				} catch(IOException e) {
					System.out.println("[ERROR] :: Error processing the log file at path: " + logFile);
				}
			} else {
				System.out.println("[ERROR] :: Log file does not exists in path: " + logFile);
			}
		} else {
			System.out.println("[ERROR] :: Log file path argument is empty");
		}
	}
		
	public void processLogLine(String logLine, Map<String, Event> events) {
		try {
			// We parse the line to JSON and if it is not valid we catch the exception an continue with the next line
			Map<String, Object> logFileEntry = parseLogLineToJson(logLine);
			
			// Check if the entry has all the minimum attributes informed
			if(checkEntryKeyValue(logFileEntry, "id") 
					&& checkEntryKeyValue(logFileEntry, "state")
					&& checkEntryKeyValue(logFileEntry, "timestamp")) {
				
				Event event;
				String eventID = (String) logFileEntry.get("id");
				Long eventTimestamp = (Long) logFileEntry.get("timestamp");	
				String eventState = (String) logFileEntry.get("state");
				
				if(events.containsKey(eventID)) {
					event = events.get(eventID);
				} else {
					// If the event exists in the database means that the current log entry is duplicated, so we discard it
					event = eventRepository.findByEventID(eventID).isEmpty() ? new Event(eventID) : null;
				}
				
				if(event != null) {
					event.setHost(checkEntryKeyValue(logFileEntry, "host") ? (String) logFileEntry.get("host") : null);
					event.setType(checkEntryKeyValue(logFileEntry, "type") ? (String) logFileEntry.get("type") : null);
					
					// If the stored event in memory has the same time informed means that the current log entry is duplicated, so we discard it
					if(eventState.equals("STARTED")) {
						if(event.getEventStartTime() != null) {
							System.out.println("[WARNING] :: Duplicated starting event with ID " + eventID);
							return;
						} else {
							event.setEventStartTime(eventTimestamp);
						}
					} else {
						if(event.getEventEndTime() != null) {
							System.out.println("[WARNING] :: Duplicated finishing event with ID " + eventID);
							return;
						} else {
							event.setEventEndTime(eventTimestamp);
						}
					}
					
					if(event.getEventStartTime() != null && event.getEventEndTime() != null) {
						long duration = event.getEventEndTime() - event.getEventStartTime();
						
						// If the registered end data is earlier than the start data we discard the entry
						if(duration > 0) { 
							if(event.getHost() == null) event.setHost(checkEntryKeyValue(logFileEntry, "host") ? (String) logFileEntry.get("host") : null);
							if(event.getType() == null) event.setType(checkEntryKeyValue(logFileEntry, "type") ? (String) logFileEntry.get("type") : null);
							event.setDuration(duration);
							event.setAlert(duration > 4 ? true : false);
							
							eventRepository.save(event);
							events.remove(eventID);
						} else {
							System.out.println("[WARNING] :: Event with ID has an ending timestamp earlier than the starting timestamp");
							events.remove(eventID);
						}
					} else {
						events.put(eventID, event);
					}
				} else {
					System.out.println("[WARNING] :: Duplicated event with ID " + eventID);
				}
			}							
		} catch(JsonParseException e) {
			System.out.println("[ERROR] :: Error parsing log entry to JSON with error: " + e.getMessage());
		} catch(NumberFormatException e) {
			System.out.println("[ERROR] :: Error parsing log entry value to Long with error: " + e.getMessage());
		} catch(Exception e) {
			System.out.println("[ERROR] :: Error inserting log entry to database with error: " + e.getMessage());
		}
	}
	
	public boolean existsLogFile(String logFile) {
		if(logFile == null || logFile.equals("")) return false;
		
		return Files.exists(Paths.get(logFile), LinkOption.values());
	}
	
	public Map<String, Object> parseLogLineToJson(String logLine) throws JsonParseException {
		return new BasicJsonParser().parseMap(logLine);
	}
	
	private boolean checkEntryKeyValue(Map<String, Object> logFileEntry, String key) {
		boolean isValid = false;
		
		if(logFileEntry.containsKey(key) 
				&& logFileEntry.get(key) != null) {
			if(logFileEntry.get(key) instanceof String) {
				if(!((String) logFileEntry.get(key)).equals("")) isValid = true;
			} else {
				isValid = true;
			}
		}
		
		return isValid;
	}
}
