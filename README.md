Ticketing application working with functionality mentioned in the requirements document, built using Spring 4, Hibernate 4, MySQL.

Executing the program:
-----------------------
*	To build the war, execute ant build with file build.xml.
*	Create database schema ("ticketing") in MYSQL, with the DB scripts found in db/DB_Mysql_DDL_and_masterData.sql.
*	To deploy, copy the war to the "webapps" in Tomcat or "standalone/deployments" directory of Jboss.

**Note: The program has been deployed in Open Shift (with Tomcat and MySQL stack), and is available at 
	http://jbossews-rajen.rhcloud.com/Ticketing/

Architecture:
-----------------------
*	Spring MVC -> Service -> DB (MYSQL).
*	LDM can be found at design/ER_Diagram_Ticketing.pdf

Assumptions:
-----------------------
*	Application has been built with a consideration that there is only one show, for which tickets are being booked. 
	But only in the screens, the show id is not captured. In domain objects, we have a provision for show id manipulation.
	So the program can be extended for booking tickets for multiple shows.
*	Active hold time for held seats have been configured as "5 minutes".

Testing:
-----------------------
*	Application is available @ http://jbossews-rajen.rhcloud.com/Ticketing/ for testing.
*	For each seat being booked, a SeatMap entity is created in the DB, which has a unique
	combination of "ShowId", "LevelId", "RowId" and "SeatId".
*	During "hold" operation, the SeatMap entities will be alloted for a User.
*	During "confirm" operation, the SeatMap entities will be confirmed for the User. Just the status is being 
	flipped to "Confirmed" from "Held".
*	While doing "hold" operation, the program always checks for previously held seats that were not confirmed
	within the specified hold time (5 minutes). If any seats are available in this category, user id (and hold id)
	in the SeatMap entities will be shifted to the new user.
*	In the screen, the list of seats Confirmed / held will be show. For example: if user selected 3 seats, the program displays as below:
		Confirmation Code - XX
		Seats - Orchestra R1 S2, Orchestra R1, S3, Orchestra R1, S4.
		*** - here "R" - refers to Row id, "S" - refers to Seat Id.
*	Some scenarios found below can be tested:
		- ending seat numbers in a row, so that next seat gets booked in very next row.
		- ending row in a level, so that program books a seat in first row in next selected level.
*       Junit test cases have been added, but due to time constraints, did not add extensively. So junit code test coverage is minimum.

Note:
-----------------------
*	Initially, thought of buildling this using AngularJS with Rest. I thought of taking this as an opportunity to do 
	HandsOn AngularJS for the first time, But due to time constraints, I developed using Spring MVC, Hindernate.
	(Again Spring MVC - I am doing for first time here. I have extensive experience in JSF or Struts, thought of doing something new, so 
	I did in Spring MVC).
*	Was curious about Open Shift, so opened an account for free, set up RubyGems for Git, SFTP, SSH, did deployment there for the first time.
*	Every bit of code found here are created from scratch and not copied from any place.


Thanks, Sriram
