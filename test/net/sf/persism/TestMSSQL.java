package net.sf.persism;

/*
 * Created by IntelliJ IDEA.
 * User: DHoward
 * Date: 9/8/11
 * Time: 6:10 AM
 */

import net.sf.persism.categories.ExternalDB;
import net.sf.persism.categories.TestContainerDB;
import net.sf.persism.dao.*;
import org.junit.ClassRule;
import org.junit.experimental.categories.Category;
import org.testcontainers.containers.MSSQLServerContainer;

import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static net.sf.persism.UtilsForTests.*;

@Category(ExternalDB.class)
public class TestMSSQL extends BaseTest {

    private static final Log log = Log.getLogger(TestMSSQL.class);

    @Override
    protected void setUp() throws Exception {

        if (BaseTest.mssqlmode) {
            connectionType = ConnectionTypes.MSSQL;
        } else {
            connectionType = ConnectionTypes.JTDS;
        }
        super.setUp();


        if (getClass().equals(TestMSSQL.class)) {
            //        BaseTest.mssqlmode = false; // to run in JTDS MODE
            log.info("SQLMODE? " + BaseTest.mssqlmode);
            con = MSSQLDataSource.getInstance().getConnection();
            log.info("PRODUCT? " + con.getMetaData().getDriverName() + " - " + con.getMetaData().getDriverVersion());

            createTables();

            session = new Session(con);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        Statement st = con.createStatement();
        try {
            st.execute("TRUNCATE TABLE Orders");
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
        try {
            st.execute("TRUNCATE TABLE Customers");
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }

        try {
            st.execute("TRUNCATE TABLE EXAMCODE");
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        } finally {
            cleanup(st, null);
        }

        // https://www.baeldung.com/java-size-of-object#:~:text=Objects%2C%20References%20and%20Wrapper%20Classes,a%20multiple%20of%204%20bytes.
        // https://stackoverflow.com/questions/52353/in-java-what-is-the-best-way-to-determine-the-size-of-an-object
        // log.warn("SESSION: " + InstrumentationAgent.getObjectSize(session));
        super.tearDown();
    }

    @Override
    protected void createTables() throws SQLException {
        log.info("createTables");
        List<String> commands = new ArrayList<String>(3);

        if (isTableInDatabase("Orders", con)) {
            commands.add("DROP TABLE Orders");

        }
        commands.add("CREATE TABLE Orders ( " +
                " [ID] [int] IDENTITY(1,1) NOT NULL, " +
                " NAME VARCHAR(30) NULL, " +
                " ROW_ID VARCHAR(30) NULL, " +
                " Customer_ID VARCHAR(10) NULL, " +
                " PAID BIT NULL, " +
                " STATUS CHAR(1) NULL, " +
                " CREATED datetime, " +
                " DATE_PAID datetime, " +
                " DATESOMETHING datetime " +
                ") ");

        commands.add("ALTER TABLE [dbo].[Orders] ADD  CONSTRAINT [DF_Orders_CREATED]  DEFAULT (getdate()) FOR [CREATED]");

        if (isTableInDatabase("Customers", con)) {
            commands.add("DROP TABLE Customers");
        }

        commands.add("CREATE TABLE Customers ( " +
                " Customer_ID varchar(10) PRIMARY KEY NOT NULL, " +
                " Company_Name VARCHAR(30) NULL, " +
                " Contact_Name VARCHAR(30) NULL, " +
                " Contact_Title VARCHAR(10) NULL, " +
                " Address VARCHAR(40) NULL, " +
                " City VARCHAR(30) NULL, " +
                " Region VARCHAR(10) NULL, " +
                " Postal_Code VARCHAR(10) NULL, " +
                " Country VARCHAR(2) NULL, " +
                " Phone VARCHAR(30) NULL, " +
                " Fax VARCHAR(30) NULL, " +
                " STATUS CHAR(1) NULL, " +
                " Date_Registered datetime  default current_timestamp, " +
                " Date_Of_Last_Order DATE, " +
                " TestLocalDate datetime, " +
                " TestLocalDateTime datetime " +
                ") ");

        if (isTableInDatabase("TABLENOPRIMARY", con)) {
            commands.add("DROP TABLE TABLENOPRIMARY");
        }

        commands.add("CREATE TABLE TABLENOPRIMARY ( " +
                " ID INT, " +
                " Name VARCHAR(30), " +
                " Field4 VARCHAR(30), " +
                " Field5 DATETIME " +
                ") ");

        if (isTableInDatabase("DumbTableStringAutoInc", con)) {
            commands.add("DROP TABLE DumbTableStringAutoInc");
        }

        commands.add("CREATE TABLE DumbTableStringAutoInc ( " +
                " ID VARCHAR(10)," +
                // "ID VARCHAR(10) IDENTITY(1,1) NOT NULL," +
                " Description VARCHAR(60) )");

        if (isTableInDatabase("Contacts", con)) {
            commands.add("DROP TABLE Contacts");
        }

        String sql = "CREATE TABLE [dbo].[Contacts]( " +
                "   [identity] [uniqueidentifier] NOT NULL, " +
                "   [PartnerID] [uniqueidentifier] NULL, " +
                "   [Type] [char](2) NOT NULL, " +
                "   [Firstname] [nvarchar](50) NULL, " +
                "   [Lastname] [nvarchar](50) NULL, " +
                "   [ContactName] [nvarchar](50) NULL, " +
                "   [Company] [nvarchar](50) NULL, " +
                "   [Division] [nvarchar](50) NULL, " +
                "   [Email] [nvarchar](50) NULL, " +
                "   [Address1] [nvarchar](50) NULL, " +
                "   [Address2] [nvarchar](50) NULL, " +
                "   [City] [nvarchar](50) NULL, " +
                "   [StateProvince] [nvarchar](50) NULL, " +
                "   [ZipPostalCode] [varchar](10) NULL, " +
                "   [Country] [nvarchar](50) NULL, " +
                "   [DateAdded] [smalldatetime] NULL, " +
                "   [LastModified] [datetime] NULL, " +
                "   [Notes] [text] NULL, " +
                "   [Status] [tinyint], " + // NOT NULL CHECK ([Status] >= 0 AND [Status] <= 10)
                "   [AmountOwed] [float] NULL, " +
                "   [BigInt] [DECIMAL](20) NULL, " +
                "   [SomeDate] [datetime2] NULL, " +
                "   [TestInstant] [datetime2] NULL, " +
                "   [TestInstant2] [datetime] NULL, " +
                "   [WhatMiteIsIt] [time](7) NULL, " +
                "   [WhatTimeIsIt] [time](7) NULL, " +
                " CONSTRAINT [PK_Contacts] PRIMARY KEY CLUSTERED  " +
                "( " +
                "   [identity] ASC " +
                ")WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY] " +
                ") ON [PRIMARY] TEXTIMAGE_ON [PRIMARY] ";
        commands.add(sql);

        sql = "ALTER TABLE [dbo].[Contacts] ADD  CONSTRAINT [DF_Contacts_identity]  DEFAULT (newid()) FOR [identity]";
        commands.add(sql);

        if (isTableInDatabase("EXAMCODE", con)) {
            commands.add("DROP TABLE EXAMCODE");
        }

        sql = "CREATE TABLE [dbo].[EXAMCODE]( " +
                "   [ExamCode_no] [int] IDENTITY(1,1) NOT NULL, " +
                "   [ExamType_No] [int] NULL, " +
                "   [Desc_e] [varchar](60) NULL, " +
                "   [Desc_f] [varchar](60) NULL, " +
                "   [ExamSubType] [varchar](20) NULL, " +
                "   [Points] [numeric](10, 3) NULL, " +
                "   [ActCodeForInjection] [varchar](5) NULL, " +
                "   [AbdomenSpineOrOther] [varchar](1) NULL, " +
                "   [AccompanyingExamCode_No] [int] NULL, " +
                "   [AcExamCode_No2] [int] NULL, " +
                "   [AcExamCode_No3] [int] NULL, " +
                "   [SideRequired] [bit] NULL, " +
                "   [StatCode] [char](20) NULL, " +
                "   [SuppressRole7ForUltrasound] [bit] NULL, " +
                "   [BodyPartNo] [int] NULL, " +
                "   [PreferredAptDuration] [numeric](3, 0) NULL, " +
                "   [PrepInstructions] [text] NULL, " +
                "   [AllowInRooms] [varchar](60) NULL, " +
                "   [ReservationType] [int] NULL, " +
                "   [ProfessionalFeeCode] [varchar](10) NULL, " +
                "   [ProfessionalFeeCode1] [varchar](10) NULL, " +
                "   [ProfessionalFeeCode2] [varchar](10) NULL, " +
                "   [ProfessionalFeeCode3] [varchar](10) NULL, " +
                "   [ProfessionalFeeCode4] [varchar](10) NULL, " +
                "   [TechnicalFeeCode] [varchar](11) NULL, " +
                "   [TechnicalFeeCode1] [varchar](11) NULL, " +
                "   [TechnicalFeeCode2] [varchar](11) NULL, " +
                "   [TechnicalFeeCode3] [varchar](11) NULL, " +
                "   [TechnicalFeeCode4] [varchar](11) NULL, " +
                "   [ICD9RequiredForBilling] [varchar](1) NULL, " +
                "   [ProFeeUnits] [numeric](2, 0) NULL, " +
                "   [ProFeeUnits1] [numeric](2, 0) NULL, " +
                "   [ProFeeUnits2] [numeric](2, 0) NULL, " +
                "   [ProFeeUnits3] [numeric](2, 0) NULL, " +
                "   [ProFeeUnits4] [numeric](2, 0) NULL, " +
                "   [TechFeeUnits] [numeric](2, 0) NULL, " +
                "   [TechFeeUnits1] [numeric](2, 0) NULL, " +
                "   [TechFeeUnits2] [numeric](2, 0) NULL, " +
                "   [TechFeeUnits3] [numeric](2, 0) NULL, " +
                "   [TechFeeUnits4] [numeric](2, 0) NULL, " +
                "   [NumberOfUnitsToBill] [numeric](2, 0) NULL, " +
                "   [QuickNormalText] [text] NULL, " +
                "   [CIHIGroupCode] [varchar](2) NULL, " +
                "   [ProfessionalFeeCode5] [varchar](10) NULL, " +
                "   [ProfessionalFeeCode6] [varchar](10) NULL, " +
                "   [TechnicalFeeCode5] [varchar](10) NULL, " +
                "   [TechnicalFeeCode6] [varchar](10) NULL, " +
                "   [ProFeeUnits5] [numeric](2, 0) NULL, " +
                "   [ProFeeUnits6] [numeric](2, 0) NULL, " +
                "   [TechFeeUnits5] [numeric](2, 0) NULL, " +
                "   [TechFeeUnits6] [numeric](2, 0) NULL, " +
                "   [EnableBirads] [bit] NULL, " +
                "   [PrintPatientLetter] [bit] NULL, " +
                "   [InstructionsForStaff] [text] NULL, " +
                "   [Retired] [bit] NULL, " +
                "   [ExternalID1] [varchar](32) NULL, " +
                "   [ExternalID2] [varchar](32) NULL, " +
                "   [ExternalID3] [varchar](32) NULL, " +
                "   [MaxDaysAgoForSameProcedure] [int] NULL, " +
                "   [RepeatWarningThresholdDays] [char](5) NULL, " +
                "   [MammoType] [varchar](1) NULL, " +
                "   [ProximityWarningHours] [int] NULL, " +
                "   [ProximityWarningMessage] [text] NULL, " +
                "   [ScheduleWithPhases] [bit] NULL, " +
                "   [AllowRefMdsAndNursesToSchedule] [bit] NULL, " +
                "   [NumberOfPhases] [int] NULL, " +
                "   [ExamCount] [int] NULL, " +
                "   [M1] [int] NULL, " +
                "   [M2] [int] NULL, " +
                "   [M3] [int] NULL, " +
                "   [M4] [int] NULL, " +
                "   [M5] [int] NULL, " +
                "   [InstructionsForStaffVisibility] [varchar](8) NULL, " +
                "   [DefaultImpression] [varchar](60) NULL, " +
                "   [ExcludeFromOtherPriors] [bit] NULL, " +
                "   [ExcludeFromGroupable] [bit] NULL, " +
                "   [DefaultModifiers] [varchar](15) NULL, " +
                "   [AlwaysPrintDoNotFaxReport] [bit] NULL, " +
                "   [RequiresProtocol] [bit] NULL, " +
                "   [NeverRequiresInterpretation] [bit] NULL, " +
                "   [ProfFeeDescrip] [varchar](200) NULL, " +
                "   [ProfFeeDescrip1] [varchar](200) NULL, " +
                "   [ProfFeeDescrip2] [varchar](200) NULL, " +
                "   [ProfFeeDescrip3] [varchar](200) NULL, " +
                "   [ProfFeeDescrip4] [varchar](200) NULL, " +
                "   [ProfFeeDescrip5] [varchar](200) NULL, " +
                "   [ProfFeeDescrip6] [varchar](200) NULL, " +
                "   [TechFeeDescrip] [varchar](200) NULL, " +
                "   [TechFeeDescrip1] [varchar](200) NULL, " +
                "   [TechFeeDescrip2] [varchar](200) NULL, " +
                "   [TechFeeDescrip3] [varchar](200) NULL, " +
                "   [TechFeeDescrip4] [varchar](200) NULL, " +
                "   [TechFeeDescrip5] [varchar](200) NULL, " +
                "   [TechFeeDescrip6] [varchar](200) NULL, " +
                "   [PromptTech4Supplies] [bit] NULL, " +
                "   [SCMUniqueID] [varchar](50) NULL, " +
                " CONSTRAINT [PK_EXAMCODE] PRIMARY KEY CLUSTERED  " +
                "( " +
                "   [ExamCode_no] ASC " +
                ")WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, FILLFACTOR = 90) ON [PRIMARY] " +
                ") ON [PRIMARY] TEXTIMAGE_ON [PRIMARY] ";
        commands.add(sql);

        if (isTableInDatabase("EXAMS", con)) {
            commands.add("DROP TABLE EXAMS");
        }

        sql = "CREATE TABLE [dbo].[EXAMS]( " +
                "   [ExamID] [int] IDENTITY(1,1) NOT NULL, " +
                "   [Accession_no] [varchar](20) NULL, " +
                "   [Patient_no] [int] NULL, " +
                "   [RequestedBy] [int] NULL, " +
                "   [DateRequested] [datetime] NULL, " +
                "   [TimeRequested] [varchar](4) NULL, " +
                "   [ExamType_No] [int] NULL, " +
                "   [ExamStatus_No] [int] NULL, " +
                "   [MasterStatus] [varchar](10) NULL, " +
                "   [CancelReason] [int] NULL, " +
                "   [Room_No] [int] NULL, " +
                "   [ExamDate] [datetime] NULL, " +
                "   [StartTime] [varchar](4) NULL, " +
                "   [EndTime] [varchar](4) NULL, " +
                "   [BookHist_No] [int] NULL, " +
                "   [Technician] [int] NULL, " +
                "   [SourceOfReferral] [int] NULL, " +
                "   [PatientLocation] [int] NULL, " +
                "   [BillStatus] [varchar](10) NULL, " +
                "   [DiagnosisCodes] [varchar](254) NULL, " +
                "   [EnteredBy] [int] NULL, " +
                "   [Abnormality] [char](1) NULL, " +
                "   [TransferredInFrom] [int] NULL, " +
                "   [ExamCode_no] [int] NULL, " +
                "   [ViewOrTarget] [int] NULL, " +
                "   [SuspectedDiagnosis] [int] NULL, " +
                "   [SignOrSymptom] [int] NULL, " +
                "   [BodyStructureNo] [int] NULL, " +
                "   [Radiologist] [int] NULL, " +
                "   [AppointmentNote] [varchar](1024) NULL, " +
                "   [ClinicalNotes] [text] NULL, " +
                "   [TechNotes] [text] NULL, " +
                "   [PriorityNo] [int] NULL, " +
                "   [Pregnant] [char](1) NULL, " +
                "   [FacilityNo] [int] NULL, " +
                "   [AccountingNumber] [varchar](12) NULL, " +
                "   [RecommendedExam] [int] NULL, " +
                "   [PathologyResultsCategoryNo] [int] NULL, " +
                "   [PathologyResultsText] [text] NULL, " +
                "   [BiradsNo] [int] NULL, " +
                "   [LockDateAndTime] [datetime] NULL, " +
                "   [LockedBy] [int] NULL, " +
                "   [BillMethodNo] [int] NULL, " +
                "   [ReferralDate] [datetime] NULL, " +
                "   [ImpressionNo] [int] NULL, " +
                "   [ExternalID] [varchar](32) NULL, " +
                "   [Iris] [int] NULL, " +
                "   [sideOrLevelNo] [int] NULL, " +
                "   [RecommendedExamCreated] [int] NULL, " +
                "   [NodalStatusNo] [int] NULL, " +
                "   [TumorSizeNo] [int] NULL, " +
                "   [BiopsyTypeNo] [int] NULL, " +
                "   [FilmLocation] [int] NULL, " +
                "   [FilmHomeLocation] [int] NULL, " +
                "   [FilmFolderNo] [int] NULL, " +
                "   [DefaultFilmLocation] [int] NULL, " +
                "   [trackingNote] [text] NULL, " +
                "   [ExternalID2] [varchar](32) NULL, " +
                "   [Transportation] [varchar](1) NULL, " +
                "   [ImagesTransferredToStorage] [bit] NULL, " +
                "   [LMP] [datetime] NULL, " +
                "   [BillTechnicalFeesOnly] [bit] NULL, " +
                "   [AppointmentConfirmed] [bit] NULL, " +
                "   [ReportChangeDateTime] [datetime] NULL, " +
                "   [ProtocoledDate] [datetime] NULL, " +
                "   [Previous_ExamStatus_No] [int] NULL, " +
                "   [Field1] [numeric](9, 2) NULL, " +
                "   [Field2] [numeric](12, 2) NULL, " +
                "   [Field4] [varchar](80) NULL, " +
                "   [Field5] [varchar](40) NULL, " +
                "   [Field6] [bit] NULL, " +
                "   [Field7] [datetime] NULL, " +
                "   [Field8] [text] NULL, " +
                "   [Field9] [numeric](9, 2) NULL, " +
                "   [Field15] [text] NULL, " +
                "   [Field16] [text] NULL, " +
                "   [Field17] [datetime] NULL, " +
                "   [Field19] [varchar](40) NULL, " +
                "   [Field20] [varchar](40) NULL, " +
                "   [Field21] [varchar](40) NULL, " +
                "   [Field18] [varchar](40) NULL, " +
                "   [Field24] [varchar](40) NULL, " +
                "   [Field34] [text] NULL, " +
                "   [SurgicalPathologyNo] [int] NULL, " +
                "   [ProtocoledBy] [int] NULL, " +
                "   [Biohazard] [text] NULL, " +
                "   [PrimaryPolicyNo] [int] NULL, " +
                "   [SecondaryPolicyNo] [int] NULL, " +
                "   [PrimaryAuthorization] [varchar](20) NULL, " +
                "   [SecondaryAuthorization] [varchar](20) NULL, " +
                "   [Field42] [varchar](40) NULL, " +
                "   [Modifiers] [varchar](15) NULL " +
                ") ON [PRIMARY] TEXTIMAGE_ON [PRIMARY] ";

        commands.add(sql);

        if (isTableInDatabase("ROOMS", con)) {
            commands.add("DROP TABLE ROOMS");
        }
        sql = "CREATE TABLE [dbo].[ROOMS]( " +
                "   [Room_No] [int] IDENTITY(1,1) NOT NULL, " +
                "   [Desc_e] [varchar](20) NULL, " +
                "   [Desc_f] [varchar](20) NULL, " +
                "   [StartTime1] [varchar](4) NULL, " +
                "   [EndTime1] [varchar](4) NULL, " +
                "   [StartTime2] [varchar](4) NULL, " +
                "   [EndTime2] [varchar](4) NULL, " +
                "   [StartTime3] [varchar](4) NULL, " +
                "   [EndTime3] [varchar](4) NULL, " +
                "   [StartTime4] [varchar](4) NULL, " +
                "   [EndTime4] [varchar](4) NULL, " +
                "   [StartTime5] [varchar](4) NULL, " +
                "   [EndTime5] [varchar](4) NULL, " +
                "   [StartTime6] [varchar](4) NULL, " +
                "   [EndTime6] [varchar](4) NULL, " +
                "   [StartTime7] [varchar](4) NULL, " +
                "   [EndTime7] [varchar](4) NULL, " +
                "   [Intervals] [numeric](2, 0) NULL, " +
                "   [ExamType_No] [int] NULL, " +
                "   [ExternalID] [varchar](16) NULL, " +
                "   [FacilityNo] [int] NULL, " +
                "   [ProcsAllowed] [varchar](1) NULL, " +
                "   [ppn_room] [varchar](25) NULL, " +
                "   [DefaultCostcenterNo] [int] NULL, " +
                "   [EnableFilmTracking] [bit] NULL, " +
                "   [Retired] [bit] NULL, " +
                "   [DefaultFilmLocation] [int] NULL, " +
                "   [FilmHomeLocation] [int] NULL, " +
                "   [DigitalImaging] [bit] NULL, " +
                "   [Weird$#@] [nchar](10) NULL, " +
                " CONSTRAINT [PK_ROOMS] PRIMARY KEY CLUSTERED  " +
                "( " +
                "   [Room_No] ASC " +
                ")WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, FILLFACTOR = 90) ON [PRIMARY] " +
                ") ON [PRIMARY]";
        commands.add(sql);


        sql = "CREATE TABLE [dbo].[USERS]( " +
                "   [User_No] [int] IDENTITY(1,1) NOT NULL, " +
                "   [UserCode] [varchar](23) NULL, " +
                "   [UserPass] [varchar](32) NULL, " +
                "   [Name] [varchar](50) NULL, " +
                "   [PasswordLastChg] [datetime] NULL, " +
                "   [Status] [varchar](1) NULL, " +
                "   [LastLogin] [datetime] NULL, " +
                "   [TypeOfUser] [varchar](1) NULL, " +
                "   [License_No] [varchar](20) NULL, " +
                "   [BillingGroup] [varchar](30) NULL, " +
                "   [Department] [int] NULL, " +
                "   [Phone] [text] NULL, " +
                "   [Street1] [varchar](50) NULL, " +
                "   [Street2] [varchar](50) NULL, " +
                "   [City] [varchar](30) NULL, " +
                "   [State] [varchar](2) NULL, " +
                "   [Zip] [varchar](10) NULL, " +
                "   [Country] [varchar](30) NULL, " +
                "   [PreferredLanguage] [varchar](1) NULL, " +
                "   [IpAddress] [varchar](15) NULL, " +
                "   [LastWebActivity] [varchar](15) NULL, " +
                "   [PasswordViolation] [int] NULL, " +
                "   [PaLayout_no] [int] NULL, " +
                "   [LastURL] [varchar](254) NULL, " +
                "   [EmailAddress] [varchar](100) NULL, " +
                "   [FaxNumber] [varchar](20) NULL, " +
                "   [PasswordReminder] [varchar](60) NULL, " +
                "   [currentMachineLoggedInto] [varchar](20) NULL, " +
                "   [FacilityNO] [int] NULL, " +
                "   [License_No2] [varchar](20) NULL, " +
                "   [Title] [varchar](10) NULL, " +
                "   [Suffix] [varchar](50) NULL, " +
                "   [AmountOwed] [money] NULL, " +
                "   [AmountOwedAfterHeadRemoval] [smallmoney] NULL, " +
                "   [SpecialityNo] [int] NULL, " +
                "   [AlertWhenRadSendsNoteToPhys] [text] NULL, " +
                "   [OldUserNo] [int] NULL, " +
                "   [TypeOfRadiologist] [varchar](1) NULL, " +
                "   [npi] [varchar](10) NULL, " +
                "   [PermissionTemplate_ID] [int] NULL, " +
                "   [forceUserToChangePwd] [bit] NULL, " +
                "   [defaultState] [varchar](2) NULL, " +
                "   [defaultCity] [varchar](30) NULL, " +
                "   [defaultCountry] [varchar](30) NULL, " +
                "   [clearUserCode] [bit] NULL, " +
                "   [residentsLevelOfExperience] [varchar](1) NULL, " +
                "   [SomeDate] [datetimeoffset](7) NULL " +
                ") ON [PRIMARY] TEXTIMAGE_ON [PRIMARY] ";

        if (isTableInDatabase("USERS", con)) {
            commands.add("DROP TABLE USERS");
        }
        commands.add(sql);

        executeCommands(commands, con);


        // TODO MSSQL has datetime2 datetime etc.. add some extras for that
        if (UtilsForTests.isTableInDatabase("DateTestLocalTypes", con)) {
            executeCommand("DROP TABLE DateTestLocalTypes", con);
        }

        sql = "CREATE TABLE DateTestLocalTypes ( " +
                " ID INT, " +
                " Description VARCHAR(100), " +
                " DateOnly DATE, " +
                " TimeOnly TIME," +
                " DateAndTime DATETIME) ";

        executeCommand(sql, con);

        if (UtilsForTests.isTableInDatabase("DateTestSQLTypes", con)) {
            executeCommand("DROP TABLE DateTestSQLTypes", con);
        }

        sql = "CREATE TABLE DateTestSQLTypes ( " +
                " ID INT, " +
                " Description VARCHAR(100), " +
                " DateOnly DATE, " +
                " TimeOnly TIME," +
                " UtilDateAndTime DATETIME," +
                " DateAndTime DATETIME) ";

        executeCommand(sql, con);


    }

    @Override
    public void testContactTable() throws SQLException {

        super.testContactTable();

        // This case will not work.
        Contact barney = new Contact();
        //contact.setIdentity(UUID.randomUUID());
        barney.setFirstname("Barney");
        barney.setLastname("Rubble");
        barney.setDivision("DIVISION X");
        barney.setLastModified(new Timestamp(System.currentTimeMillis() - 100000000l));
        barney.setContactName("Fred Flintstone");
        barney.setAddress1("123 Sesame Street");
        barney.setAddress2("Appt #0 (garbage can)");
        barney.setCompany("Grouch Inc");
        barney.setCountry("US");
        barney.setCity("Philly?");
        barney.setType("X");
        barney.setStatus((byte) 1);
        barney.setDateAdded(new Date(System.currentTimeMillis()));
        barney.setAmountOwed(100.23f);
        barney.setNotes("B:AH B:AH VBLAH\r  BLAH BLAY!");

        int count = session.query(Contact.class, "select * from Contacts").size();

        boolean failed = false;
        try {
            session.insert(barney);
        } catch (PersismException e) {
            failed = true;
            assertEquals("message s/b 'Non-auto inc generated primary keys are not supported. Please assign your primary key value before performing an insert.'",
                    "Non-auto inc generated primary keys are not supported. Please assign your primary key value before performing an insert.",
                    e.getMessage());
        }
        assertTrue(failed);

        assertEquals("should have same count in Contacts",
                count,
                session.query(Contact.class, "select * from Contacts").size());

        log.info(session.query(Contact.class, "select * from Contacts"));

    }


    public void testProcedure() throws SQLException {

        Procedure procx = new Procedure();
        procx.setDescription("COW2");
        session.insert(procx);

        boolean fail = false;
        try {
            con.setAutoCommit(false);
            session.query(Procedure.class, "SELECT ExamCode_No, EXAMTYPE_NO, DESC_E FROM EXAMCODE");
        } catch (PersismException e) {
            fail = true;
            assertTrue("exception should be 'Object class net.sf.persism.dao.Procedure was not properly initialized.'", e.getMessage().startsWith("Object class net.sf.persism.dao.Procedure was not properly initialized"));
            con.setAutoCommit(true);
        }
        assertTrue(fail);

        long now = System.currentTimeMillis();
        List<Procedure> list = session.query(Procedure.class, "SELECT * FROM EXAMCODE");
        log.info("time to read procs 1: " + (System.currentTimeMillis() - now));
        log.info(list.toString());

        now = System.currentTimeMillis();
        list = session.query(Procedure.class, "SELECT * FROM EXAMCODE");
        log.info("time to read procs 2: " + (System.currentTimeMillis() - now));

        for (Procedure procedure : list) {
            log.info(procedure.getExamCodeNo() + " " + procedure.getDescription() + " " + procedure.getModalityId());
        }

        log.info("time to display procs: " + (System.currentTimeMillis() - now));

        Procedure proc1 = new Procedure();
        proc1.setDescription("COW");
        session.insert(proc1);

        // Instantiate a new and do an update
        Procedure procedure = new Procedure();
        procedure.setExamCodeNo(proc1.getExamCodeNo());
        procedure.setDescription(proc1.getDescription());
        session.update(procedure);

        session.fetch(procedure);
        procedure.setDescription(null);
        session.update(procedure);
        session.fetch(procedure);
        log.warn("NULL? " + procedure.getDescription());


        assertTrue("proc1 should have an id? ", proc1.getExamCodeNo() > 0);

        // test fetch
        Procedure proc2 = session.fetch(Procedure.class, "select * from EXAMCODE WHERE ExamCode_No=?", 2);
        assertNotNull("proc2 should be found ", proc2);

        Procedure proc3 = session.fetch(Procedure.class, "select * from EXAMCODE WHERE ExamCode_No=?", -99);
        assertNull("proc3 should NOT be found ", proc3);

        Procedure proc4 = session.fetch(Procedure.class, "select * from EXAMCODE WHERE ExamCode_No=?", 2);
        assertNotNull("proc4 should be found ", proc4);


        now = System.currentTimeMillis();
        list = session.query(Procedure.class, "select * from EXAMCODE WHERE ExamCode_No=?", 2);
        log.info("time to read procs 3: " + (System.currentTimeMillis() - now));
        assertEquals("should only have 1 proc in the list", 1, list.size());

        Procedure proc = list.get(0);
        int result = session.delete(proc);
        assertEquals("Should be 1 for delete", 1, result);
    }

    public void testRoom() {
        Room roomX = new Room();
        roomX.setDescription("room 1");
        roomX.setIntervals(new BigDecimal(10));
        roomX.setWeird("werid?");
        roomX.setJunk("junk");
        session.insert(roomX);

        long now = System.currentTimeMillis();
        List<Room> list = session.query(Room.class, "SELECT Room_no, Desc_E, Intervals, [Weird$#@]  FROM ROOMS");
        log.info("time to read rooms: " + (System.currentTimeMillis() - now) + " size: " + list.size());

        now = System.currentTimeMillis();
        list = session.query(Room.class, "SELECT *  FROM ROOMS"); // Room_no, Desc_E, Intervals
        log.info("time to read rooms again: " + (System.currentTimeMillis() - now));

        now = System.currentTimeMillis();
        for (Room room : list) {
            log.info(room.getRoomNo() + " " + room.getDescription());
        }
        log.info("time to display rooms : " + (System.currentTimeMillis() - now));
    }

    public void testJunk() {
        // query = new Query(con);
        int n = getClass().getName().lastIndexOf(".");
        String className;
        if (n > -1) {
            className = getClass().getName().substring(n + 1);
        } else {
            className = getClass().getName();
        }

        log.info("|" + className + "| ---- " + getClass().getName());

        List<String> list = new ArrayList<String>(3);
        list.add("Blobbo");
        list.add("Cracked");
        list.add("Dumbo");

        // Convert a collection to Object[], which can store objects
        // of any type.
        Object[] ol = list.toArray();
        log.info("Array of Object has length " + ol.length);

        // This would throw an ArrayStoreException if the line
        // "list.add(new Date())" above were uncommented.
        String[] sl = list.toArray(new String[0]);
        log.info("Array of String has length " + sl.length + " " + Arrays.asList(sl));
    }

    public void testOutsideEnum() throws Exception {
        String sql = "INSERT INTO [Customers] ([Customer_ID], [Company_Name], [Contact_Name], [Contact_Title], " +
                "[Address], [City], [Region], [Postal_Code], [Country], [Phone], " +
                "[Fax], [STATUS], [Date_Of_Last_Order]) VALUES ( ? ,  ? ,  ? ,  ? ,  ? ,  ? ,  ? ,  ? ,  ? ,  ? ,  ? ,  ? ,  ? )";

        session.execute(sql, "X", "Name", "Contact", "Title", "Address", "City", "NOTAREGION", "CODe", "CA", "1", "2", "3", null);

        boolean failed = false;
        try {
            Customer c1 = new Customer();
            c1.setCustomerId("X");
            session.fetch(c1);

        } catch (Exception e) {
            failed = true;

            log.info(e.getMessage(), e);

            assertEquals("message s/b 'Object class net.sf.persism.dao.Customer. Column: Region Type of property: class net.sf.persism.dao.Regions - Type read: class java.lang.String VALUE: NOTAREGION'",
                    "Object class net.sf.persism.dao.Customer. Column: Region Type of property: class net.sf.persism.dao.Regions - Type read: class java.lang.String VALUE: NOTAREGION",
                    e.getMessage());
        }
        assertTrue(failed);

    }

    public void testStoredProc() throws Exception {
        if (isProcedureInDatabase("spCustomerOrders", con)) {
            executeCommand("DROP PROCEDURE spCustomerOrders", con);
        }
        // DO NOT remove line feeds
        String sql = "CREATE PROCEDURE [dbo].[spCustomerOrders]\n" +
                "   @custId varchar(10)\n" +
                "AS\n" +
                "BEGIN\n" +
                "   -- SET NOCOUNT ON added to prevent extra result sets from\n" +
                "   -- interfering with SELECT statements.\n" +
                "   SET NOCOUNT ON;\n" +
                "\n" +
                "    -- Insert statements for procedure here\n" +
                "   SELECT c.Customer_ID, c.Company_Name, o.ID Order_ID, \n" +
                "      o.Name AS Description, o.Date_Paid, o.Created AS DateCreated, o.PAID \n" +
                "        FROM ORDERS o\n" +
                "        JOIN Customers c ON o.Customer_ID = c.Customer_ID\n" +
                "   WHERE c.Customer_ID = @custId        \n" +
                "END";
        executeCommand(sql, con);

        Customer c1 = new Customer();
        c1.setCustomerId("123");
        c1.setCompanyName("ABC INC");
        c1.setRegion(Regions.East);
        session.insert(c1);

        Customer cx = new Customer();
        cx.setCustomerId("123");
        cx.setCompanyName("ABC INC");
        cx.setRegion(Regions.East);
        cx.setAddress("asasasas");
        cx.setStatus('e');
        session.update(cx);

        assertNotNull("Should be defaulted", c1.getDateRegistered());

        Customer c2 = new Customer();
        c2.setCustomerId("456");
        c2.setCompanyName("XYZ INC");
        session.insert(c2);

        Order order;
        order = DAOFactory.newOrder(con);
        order.setCustomerId("123");
        order.setName("ORDER 1");
        order.setCreated(LocalDate.now());
        order.setPaid(true);
        session.insert(order);

        order = DAOFactory.newOrder(con);
        order.setCustomerId("123");
        order.setName("ORDER 2");
        order.setCreated(LocalDate.now());
        order.setPaid(false);
        session.insert(order);

        session.fetch(order);

        List<CustomerOrder> list = session.query(CustomerOrder.class, "[spCustomerOrders](?)", "123");
        log.info(list);
        // Both forms should work - the 1st is a cleaner way but this should be supported
        list = session.query(CustomerOrder.class, "{call [spCustomerOrders](?) }", "123");
        log.info(list);

        // query orders by date
        //DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        //DateTimeFormatter df = DateTimeFormatter.ISO_DATE;

        List<Order> orders = session.query(Order.class, "select * from Orders where CONVERT(varchar, created, 112) = ?", order.getCreated().format(DateTimeFormatter.ISO_LOCAL_DATE));
        log.info("ORDERS?  " + orders);

        orders = session.query(Order.class, "select * from Orders where created = ?", order.getCreated().format(DateTimeFormatter.ISO_LOCAL_DATE));
        log.info("ORDERS AGAIN?  " + orders);
    }

    public void testQuery() throws Exception {
        Procedure procedure = new Procedure();
        procedure.setDescription("proc 1");
        session.insert(procedure);

        Room room = new Room();
        room.setDescription("room 1");
        session.insert(room);

        Exam exam = new Exam();
        exam.setExamCodeNo(procedure.getExamCodeNo());
        exam.setRoomNo(room.getRoomNo());
        exam.setExamDate(new Date(System.currentTimeMillis()));
        session.insert(exam);

        String sql;
        sql = "select top 10 ExamID, p.DESC_E ProcedureDescription, r.DESC_E RoomDescription, eXaMdAtE from exams " +
                "left join EXAMCODE as p ON Exams.ExamCode_No = p.ExamCode_No " +
                "left join ROOMS as r ON Exams.Room_No = r.Room_No ";

        List<QueryResult> list = session.query(QueryResult.class, sql);
        log.info(list.toString());
        assertTrue("size should be > 0 ", list.size() > 0);

        // Try again changing case of some fields.
        sql = "select top 10 ExamID, p.Desc_E ProcedureDescription, r.Desc_E RoomDescription, ExamDate from exams " +
                "left join EXAMCODE as p ON Exams.ExamCode_No = p.ExamCode_No " +
                "left join ROOMS as r ON Exams.Room_No = r.Room_No ";

        list = session.query(QueryResult.class, sql);
        log.info(list.toString());
        assertTrue("size should be > 0 ", list.size() > 0);

        List<Integer> simpleList;
        sql = "select top 10 ExamID from exams " +
                "left join EXAMCODE as p ON Exams.ExamCode_No = p.ExamCode_No " +
                "left join ROOMS as r ON Exams.Room_No = r.Room_No ";

        simpleList = session.query(Integer.class, sql);
        log.info(simpleList.toString());
        assertTrue("size should be > 0 ", simpleList.size() > 0);
    }

    public void testAllColumnsMappedException() {

        Contact contact = new Contact();
        contact.setIdentity(UUID.randomUUID());
        contact.setFirstname("Wilma");
        contact.setLastname("Flintstone");
        contact.setContactName("Fred");
        contact.setType("CL");
        session.insert(contact);

        boolean shouldHaveFailed = false;
        try {
            session.fetch(Contact.class, "select [identity] from Contacts");
        } catch (PersismException e) {
            shouldHaveFailed = true;
            log.info(e.getMessage(), e);
            // Apparently we don't always get the same column order?
//            assertEquals("message should be ", "Object class net.sf.persism.dao.Contact was not properly initialized. Some properties not found in the queried columns. : [Company, Email, StateProvince, Address2, Lastname, PartnerID, Address1, City, Firstname, LastModified, Type, ZipPostalCode, Country, Division, DateAdded, ContactName]", e.getMessage());
        }
        assertEquals("should have failed", true, shouldHaveFailed);
    }

    public void testAdditionalPropertyNotMappedException() {

        Contact contact = new Contact();
        contact.setIdentity(UUID.randomUUID());
        contact.setFirstname("Wilma");
        contact.setLastname("Flintstone");
        contact.setContactName("Fred");
        contact.setType("CL");
        session.insert(contact);

        boolean shouldHaveFailed = false;
        try {
            session.fetch(ContactFail.class, "select * from Contacts");
        } catch (PersismException e) {
            shouldHaveFailed = true;
            log.info(e.getMessage(), e);
            assertEquals("message should be ", "Object class net.sf.persism.dao.ContactFail was not properly initialized. Some properties not initialized in the queried columns (fail).", e.getMessage());
        }

        assertEquals("should have failed", true, shouldHaveFailed);
    }

    public void testCountQuery() {

        Exam exam = new Exam();
        exam.setAccessionNo("123");
        exam.setExamDate(new java.util.Date(System.currentTimeMillis()));
        exam.setDateRequested(LocalDate.now());
        exam.setMasterStatus("x");

        session.insert(exam);
        assertTrue("id > 0", exam.getExamId() > 0);

        String sql;
        sql = "select examdate from exams";

        java.util.Date date = session.fetch(java.util.Date.class, sql);
        log.info("DATE? " + date);

        sql = "select count(*) from exams";
        int exams = session.fetch(Integer.class, sql);
        log.info("" + exams);
        assertTrue("should be > 0", exams > 0);

        sql = "select count(*) from exams where examDate > ?";
        Date d = new Date(1997 - 1900, 2, 4);
        log.info("" + d);
        exams = session.fetch(Integer.class, sql, d);
        log.info("" + exams);
        assertTrue("should be > 0", exams > 0);

    }


    public void testUpdate() {


        try {
            Procedure proc1; // = query.readObject(Procedure.class, "select * from examcode where examcode_no=3");
            proc1 = new Procedure();
            proc1.setDescription("new proc LDKJH DLKJH SLKJH");
            session.insert(proc1);

            int examCodeNo = proc1.getExamCodeNo();
            assertTrue("examcode no > 0", examCodeNo > 0);


            assertTrue("should get a proc for ?" + examCodeNo, session.fetch(proc1));


            Procedure proc2; // = query.readObject(Procedure.class, "select * from examcode where examcode_no=?", 3);
            proc2 = new Procedure();
            proc2.setExamCodeNo(examCodeNo);
            session.fetch(proc2);

            assertEquals("both procs should be the same id: 3 ", proc1.getExamCodeNo(), proc2.getExamCodeNo());

            proc1.setDescription("JUNK JUNK JUNK");

            session.update(proc1);


            //proc2 = query.readObject(Procedure.class, "select * from examcode where examcode_no=3");
            proc2.setExamCodeNo(examCodeNo);
            session.fetch(proc2);
            assertEquals("should be JUNK JUNK JUNK", "JUNK JUNK JUNK", proc2.getDescription());

            Order order = DAOFactory.newOrder(con);
            order.setName("MOO");
            session.insert(order);

            List<Order> orders = session.query(Order.class, "SELECT * FROM ORDERS");
            assertEquals("size s/b 1", 1, orders.size());

            order = orders.get(0);

            order.setName("COW");

            session.update(order);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        }

    }

    public void testInsert() {
        // query = new Query(con);
        Procedure procedure = new Procedure();
        procedure.setDescription("TEST 99 LKJHDSLKJSDH");
        procedure.setModalityId(2);
        procedure.setSomeDate(new java.util.Date());

        session.insert(procedure);
        log.info("" + procedure);
        //asseertE
    }


    public void testDetectAutoInc() {

        DumbTableStringAutoInc dumb = new DumbTableStringAutoInc();
        dumb.setDescription("test");
        session.insert(dumb);
        // should not actually do anything since there is no autoinc.
        // the table is defined with VARCHAR
        log.info(dumb);
        assertNull(dumb.getId());

        // query = new Query(con);
        try {
            Statement st = con.createStatement();
            java.sql.ResultSet rs = st.executeQuery("SELECT * FROM ORDERS WHERE 1=2");
            ResultSetMetaData rsMetaData = rs.getMetaData();
            for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
                log.info(rsMetaData.getColumnName(i));
                log.info(rsMetaData.isAutoIncrement(i));
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        }

    }

    public void testUserDAO() throws Exception {
        /*
        Notes: for DateTimeOffset
            JTDS 1.2.5, 1.3.1 reads value as TimeStamp SQL TYPE 93
            MSSQL Driver reads reads value as -155 Not defined in java.sql.Types !
            See our Types class - we'll just check for that and set it as a TimeStamp to be consistent with JTDS

            To convert back see.
            https://stackoverflow.com/questions/36405320/using-the-datetimeoffset-datatype-with-jtds
         */
        User user = new User();
        user.setDepartment(2);
        user.setName("test 1");
        user.setTypeOfUser("X");
        user.setStatus("Z");
        user.setUserName("ABC");
        user.setAmountOwed(BigDecimal.valueOf(123.567d));
        user.setAmountOwedAfterHeadRemoval(4.73f);

        assertTrue("id s/b = 0", user.getId() == 0);

        session.insert(user);

        assertTrue("id s/b > 0", user.getId() > 0);

        log.info(user);

        User user2 = new User();
        user2.setId(user.getId());
        session.fetch(user2);
        log.info(user2);
    }


    public void testNullPointerWithTableTypes() throws SQLException {
        ResultSet rs = null;
        // NULL POINTER WITH
        // http://social.msdn.microsoft.com/Forums/en-US/sqldataaccess/thread/5c74094a-8506-4278-ac1c-f07d1bfdb266
        String[] tableTypes = {"TABLE"};
        rs = con.getMetaData().getTables(null, "%", null, tableTypes);
        while (rs.next()) {
            log.info(rs.getString("TABLE_NAME"));
        }
    }


    public void testTryUDDI() throws SQLException {
        // this was a test to see if I could prepare a statement and return all columns. Nope.....

        // ensure metadata is there
        log.info(session.query(Contact.class, "select * from Contacts"));

//        String insertStatement = "INSERT INTO Customers (Customer_ID, Company_Name, Contact_Name) VALUES ( ?, ?, ? ) ";
        String insertStatement = "INSERT INTO Contacts (FirstName, LastName, Type, Status) VALUES ( ?, ?, ?, ? ) ";

        PreparedStatement st = null;
        ResultSet rs = null;

        // Map<String, ColumnInfo> columns = session.getMetaData().getColumns(Contact.class, con);
        List<String> keys = session.getMetaData().getPrimaryKeys(Contact.class, con);
//        String[] columnNames = columns.keySet().toArray(new String[0]);
        String[] columnNames = keys.toArray(new String[0]);
        st = con.prepareStatement(insertStatement, columnNames);
        //st = con.prepareStatement(insertStatement, Statement.RETURN_GENERATED_KEYS);
        st.setString(1, "Slate Quarry");
        st.setString(2, "Fred");
        st.setString(3, "X");
        st.setShort(4, (short) 10);


        int ret = st.executeUpdate();
        log.info("rows insetred " + ret);
        rs = st.getGeneratedKeys();
        log.info("resultset? " + st.getResultSet());
        ResultSetMetaData rsmd = rs.getMetaData();
        while (rs.next()) {
            //log.info("NOPE: " + rs.getString(1));

            for (int j = 1; j <= rsmd.getColumnCount(); j++) {
                log.info(j + " " + rsmd.getColumnLabel(j) + " " + rsmd.getColumnTypeName(j) + " " + rs.getObject(j));
            }
        }

        // registerOutParameter is only for out parameters to stored procs - not a general return mechanism
//        CallableStatement cs = con.prepareCall(insertStatement, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
//        cs.setString(1, "Slate Quarry");
//        cs.setString(2, "Fred");
//        cs.setString(3, "X");
//
//        cs.registerOutParameter(1, Types.VARCHAR);
//        cs.execute();
//  log.error("RESULTSET?" + cs.getResultSet());
//  if (cs.getResultSet() != null) {
//      while (cs.getResultSet().next() ) {
//          log.info("UDDI? " + rs.getObject(1));
//      }
//  }

//        PreparedStatement ps = con.prepareStatement("cklcklck");
//        ps.rer


//        PreparedStatement pstmt = con.prepareStatement("insert into some_table (some_value) values (?)", new String[]{"id"});
//        pstmt.setInt(1, 42);
//        pstmt.executeUpdate();
//        ResultSet rs  = pstmt.getGeneratedKeys();
//        UUID id = null;
//        if (rs.next()) id = rs.getObject(1, UUID.class);


    }

    @Override
    public void testAllDates() {
        super.testAllDates();
    }
}