package io.gab.challenge.logreader;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParseException;
import org.springframework.boot.test.context.SpringBootTest;

import io.gab.challenge.logreader.controller.EventController;
import io.gab.challenge.logreader.data.Event;
import io.gab.challenge.logreader.repository.EventRepository;

@SpringBootTest
class LogReaderApplicationTests {
	
	@Autowired
    private EventRepository eventRepository;
	
	@Autowired
    private EventController eventController;
	
	@Test
	public void loadingLogFile_NoExistsFile() {
		assertFalse(eventController.existsLogFile(null));  
		assertFalse(eventController.existsLogFile(""));  
	}
	
	@Test
	public void loadingLogFile_ExistsFile() {
		assertTrue(eventController.existsLogFile("src\\main\\resources\\test\\logfile.txt")); 		
	}
	
	@Test
	public void parsingJson_NotWellConstructed() {
		boolean asserted = false;
		
		try {
			eventController.parseLogLineToJson("{\"test\":\"wrong\"");
		} catch(JsonParseException e) {
			asserted = true;
		}
		
		assertFalse(asserted);
	}
	
	@Test
	public void parsingJson_WellConstructed() {
		boolean asserted = true;
		
		try {
			eventController.parseLogLineToJson("{\"test\":\"ok\"}");
		} catch(JsonParseException e) {
			asserted = false;
		}
		
		assertTrue(asserted);
	}
	
	@Test
	public void addingNewEntry_CorrectData() {
		Map<String, Event> events = new HashMap<String, Event>(); 
		
		eventController.processLogLine("{\"id\":\"test_scsmbstgrb\", \"state\":\"FINISHED\", \"timestamp\":1491377495216}", events);
		eventController.processLogLine("{\"id\":\"test_scsmbstgrb\", \"state\":\"STARTED\", \"timestamp\":1491377495213}", events);
		
		List<Event> event = eventRepository.findByEventID("test_scsmbstgrb");
		
		assertTrue((event != null && event.size() == 1));
		assertFalse(event.get(0).getAlert());
		
		eventRepository.deleteAll();
	}
	
	@Test
	public void addingNewEntry_CorrectDataWithAlert() {
		Map<String, Event> events = new HashMap<String, Event>(); 
		
		eventController.processLogLine("{\"id\":\"test_scsmbstgra\", \"state\":\"STARTED\", \"type\":\"APPLICATION_LOG\", \"host\":\"12345\", \"timestamp\":1491377495212}", events);
		eventController.processLogLine("{\"id\":\"test_scsmbstgra\", \"state\":\"FINISHED\", \"type\":\"APPLICATION_LOG\", \"host\":\"12345\", \"timestamp\":1491377495217}", events);
		
		List<Event> event = eventRepository.findByEventID("test_scsmbstgra");
		assertTrue((event != null && event.size() == 1));
		assertTrue(event.get(0).getAlert());
		
		eventRepository.deleteAll();
	}
	
	@Test
	public void addingNewEntry_OneEntry() {
		Map<String, Event> events = new HashMap<String, Event>(); 
		
		eventController.processLogLine("{\"id\":\"test_one_scsmbstgra\", \"state\":\"STARTED\", \"type\":\"APPLICATION_LOG\", \"host\":\"12345\", \"timestamp\":1491377495217}", events);
		
		List<Event> event = eventRepository.findByEventID("test_one_scsmbstgra");
		assertTrue(eventRepository.findByEventID("test_one_scsmbstgra").isEmpty());
	}
	
	@Test
	public void addingNewEntry_IncorrectData() {
		Map<String, Event> events = new HashMap<String, Event>(); 
		
		eventController.processLogLine("{\"id\":\"test_incorrect_scsmbstgra\", \"state\":\"\", \"type\":\"APPLICATION_LOG\", \"host\":\"12345\", \"timestamp\":1491377495217}", events); //Error
		eventController.processLogLine("{\"id\":\"test_incorrect_scsmbstgra\", \"state\":\"\", \"type\":\"APPLICATION_LOG\", \"host\":\"12345\", \"timestamp\":1491377495212}", events); //Error
		
		assertTrue(eventRepository.findByEventID("test_time_scsmbstgra").isEmpty());
	}
	
	@Test
	public void addingNewEntry_IncorrectDataTime() {
		Map<String, Event> events = new HashMap<String, Event>(); 
		
		eventController.processLogLine("{\"id\":\"test_time_scsmbstgra\", \"state\":\"STARTED\", \"type\":\"APPLICATION_LOG\", \"host\":\"12345\", \"timestamp\":1491377495217}", events);
		eventController.processLogLine("{\"id\":\"test_time_scsmbstgra\", \"state\":\"FINISHED\", \"type\":\"APPLICATION_LOG\", \"host\":\"12345\", \"timestamp\":1491377495212}", events);
		
		List<Event> event = eventRepository.findByEventID("test_time_scsmbstgra");
		assertTrue(eventRepository.findByEventID("test_time_scsmbstgra").isEmpty());
	}
	
	@Test
	public void addingNewEntry_DuplicatedDataTime() {
		Map<String, Event> events = new HashMap<String, Event>(); 
		
		eventController.processLogLine("{\"id\":\"test_time_duplicated_scsmbstgra\", \"state\":\"STARTED\", \"type\":\"APPLICATION_LOG\", \"host\":\"12345\", \"timestamp\":1491377495212}", events);
		eventController.processLogLine("{\"id\":\"test_time_duplicated_scsmbstgra\", \"state\":\"STARTED\", \"type\":\"APPLICATION_LOG\", \"host\":\"12345\", \"timestamp\":1491377495217}", events);
		
		assertTrue(eventRepository.findByEventID("test_time_duplicated_scsmbstgra").isEmpty());
	}
	
	@Test
	public void addingMultipleEntries_CorrectData() {
		Map<String, Event> events = new HashMap<String, Event>(); 
		
		eventController.processLogLine("{\"id\":\"scsmbstgra\", \"state\":\"STARTED\", \"type\":\"APPLICATION_LOG\", \"host\":\"12345\", \"timestamp\":1491377495212}", events);
		eventController.processLogLine("{\"id\":\"scsmbstgrb\", \"state\":\"STARTED\", \"timestamp\":1491377495213}", events);
		eventController.processLogLine("{\"id\":\"scsmbstgrc\", \"state\":\"FINISHED\", \"timestamp\":1491377495218}", events);
		eventController.processLogLine("{\"id\":\"scsmbstgra\", \"state\":\"FINISHED\", \"type\":\"APPLICATION_LOG\", \"host\":\"12345\", \"timestamp\":1491377495217}", events);
		eventController.processLogLine("{\"id\":\"scsmbstgrc\", \"state\":\"STARTED\", \"timestamp\":1491377495210}", events);
		eventController.processLogLine("{\"id\":\"scsmbstgrb\", \"state\":\"FINISHED\", \"timestamp\":1491377495216}", events);
		
		assertTrue(eventRepository.findAll().size() == 3);
		
		eventRepository.deleteAll();
	}
	
	@Test
	public void addingMultipleEntries_IncorrectData() {
		Map<String, Event> events = new HashMap<String, Event>(); 
		
		eventController.processLogLine("{\"id\":\"addingMultipleEntries_IncorrectData_scsmbstgra\", \"state\":\"STARTED\", \"type\":\"APPLICATION_LOG\", \"host\":\"12345\", \"timestamp\":1491377495212}", events);
		eventController.processLogLine("{\"id\":\"addingMultipleEntries_IncorrectData_scsmbstgrb\", \"state\":\"STARTED\", \"timestamp\":\"1491377495213\"}", events); // Error
		eventController.processLogLine("{\"id\":\"addingMultipleEntries_IncorrectData_scsmbstgrc\", \"state\":\"FINISHED\", \"timestamp\":1491377495218}", events);
		eventController.processLogLine("{\"id\":\"addingMultipleEntries_IncorrectData_scsmbstgra\", \"state\":\"FINISHED\", \"type\":\"APPLICATION_LOG\", \"host\":\"12345\", \"timestamp\":1491377495217}", events);
		eventController.processLogLine("{\"id\":\"addingMultipleEntries_IncorrectData_scsmbstgrc\", \"state\":\"\", \"timestamp\":1491377495210}", events); //Error
		eventController.processLogLine("{\"id\":\"addingMultipleEntries_IncorrectData_scsmbstgrb\", \"state\":\"FINISHED\", \"timestamp\":1491377495216}", events);
		
		assertTrue(eventRepository.findAll().size() == 1);
		
		eventRepository.deleteAll();
	}
	
	@Test
	public void addingMultipleEntries_WithDuplicatedEntry() {
		Map<String, Event> events = new HashMap<String, Event>(); 
		
		eventController.processLogLine("{\"id\":\"addingMultipleEntries_WithDuplicatedEntry_scsmbstgra\", \"state\":\"STARTED\", \"type\":\"APPLICATION_LOG\", \"host\":\"12345\", \"timestamp\":1491377495212}", events);
		eventController.processLogLine("{\"id\":\"addingMultipleEntries_WithDuplicatedEntry_scsmbstgrb\", \"state\":\"STARTED\", \"timestamp\":1491377495213}", events);
		eventController.processLogLine("{\"id\":\"addingMultipleEntries_WithDuplicatedEntry_scsmbstgrc\", \"state\":\"FINISHED\", \"timestamp\":1491377495218}", events);
		eventController.processLogLine("{\"id\":\"addingMultipleEntries_WithDuplicatedEntry_scsmbstgra\", \"state\":\"FINISHED\", \"type\":\"APPLICATION_LOG\", \"host\":\"12345\", \"timestamp\":1491377495217}", events);
		eventController.processLogLine("{\"id\":\"addingMultipleEntries_WithDuplicatedEntry_scsmbstgrc\", \"state\":\"STARTED\", \"timestamp\":1491377495210}", events);
		eventController.processLogLine("{\"id\":\"addingMultipleEntries_WithDuplicatedEntry_scsmbstgrb\", \"state\":\"FINISHED\", \"timestamp\":1491377495216}", events);
		eventController.processLogLine("{\"id\":\"addingMultipleEntries_WithDuplicatedEntry_scsmbstgrb\", \"state\":\"STARTED\", \"timestamp\":1491377495213}", events); //Duplicated
		eventController.processLogLine("{\"id\":\"addingMultipleEntries_WithDuplicatedEntry_scsmbstgrb\", \"state\":\"FINISHED\", \"timestamp\":1491377495216}", events); //Duplicated
		
		assertTrue(eventRepository.findAll().size() == 3);
		
		eventRepository.deleteAll();
	}
}
