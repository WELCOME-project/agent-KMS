package com.welcome.auxiliary;

import java.util.Arrays;
import java.util.List;

public class SlotLists {

  public static List<String> simulationSlots =
      Arrays.asList(
          "welcome:Name",
          "welcome:Gender",
          "welcome:Country"
      );

  public static List<String> personalSlots =
      Arrays.asList(
          "welcome:Name",
          "welcome:FirstSurname",
          "welcome:SecondSurname",
          "welcome:Birthday",
          "welcome:MobilePhone",
          "welcome:Landline",
          "welcome:Email",
          "welcome:City",
          "welcome:StreetName",
          "welcome:StreetType",
          "welcome:PostCode",
          "welcome:ApartmentNumber",
          "welcome:NativeLanguageName"
      );

  public static List<String> otherSlotsFull =
      Arrays.asList(
          "welcome:DrivingLicense",
          "welcome:CarPossession"
      );

  public static List<String> personalSlotsFull =
      Arrays.asList(
          "welcome:Name",
          "welcome:FirstSurname",
          "welcome:SecondSurname",
          "welcome:Birthday",
          "welcome:MobilePhone",
          "welcome:Landline",
          "welcome:Email",
          "welcome:City",
          "welcome:StreetName",
          "welcome:StreetType",
          "welcome:StreetNumber",
          //"welcome:BuildingName",
          //"welcome:Entrance",
          //"welcome:BuildingType",
          "welcome:FloorNumber",
          "welcome:ApartmentNumber",
          "welcome:PostCode",
          //"welcome:Province",
          //"welcome:Municipality",
          "welcome:NativeLanguageName"
      );

  public static List<String> addressSlots =
      Arrays.asList(
          "welcome:StreetNumber",
          "welcome:BuildingName",
          "welcome:Entrance",
          "welcome:BuildingType",
          "welcome:FloorNumber",
          "welcome:ApartmentNumber",
          "welcome:PostCode",
          "welcome:Province",
          "welcome:Municipality"
      );

  public static List<String> educationSlots =
      Arrays.asList(
          "welcome:DegreeTitle",
          "welcome:DegreeCertificate",
          "welcome:InstitutionName",
          "welcome:GraduationYear",
          "welcome:GraduationStatus",
          "welcome:CompletedCourses",
          "welcome:DegreeGrade"
      );

  // TODO Depending on the final decision
  // we may need to add one more slot here
  public static List<String> educationSlotsFull =
      Arrays.asList(
          "welcome:DegreeTitle",
          "welcome:DegreeCertificate",
          "welcome:InstitutionName",
          "welcome:GraduationYear",
          "welcome:GraduationStatus",
          "welcome:CompletedCourses",
          "welcome:DegreeGrade",
          "welcome:AdditionalEducation",
          "welcome:IntroductionEducation"
      );

  public static List<String> addEducationSlots =
      Arrays.asList(
          "welcome:DegreeTitle",
          "welcome:DegreeCertificate",
          "welcome:InstitutionName",
          "welcome:GraduationYear",
          "welcome:GraduationStatus",
          "welcome:CompletedCourses",
          "welcome:DegreeGrade",
          "welcome:AdditionalEducation"
      );

  public static List<String> courseSlotsFull =
      Arrays.asList(
          "welcome:CourseName",
          "welcome:CourseCertificate",
          "welcome:CourseStatus",
          "welcome:CourseSchool",
          "welcome:CourseSchoolType",
          "welcome:CourseYear",
          "welcome:CourseDuration",
          "welcome:CourseGrade",
          "welcome:AdditionalCourse",
          "welcome:IntroductionOtherEducation"
      );

  public static List<String> addCourseSlots =
      Arrays.asList(
          "welcome:CourseName",
          "welcome:CourseCertificate",
          "welcome:CourseStatus",
          "welcome:CourseSchool",
          "welcome:CourseSchoolType",
          "welcome:CourseYear",
          "welcome:CourseDuration",
          "welcome:CourseGrade",
          "welcome:AdditionalCourse"
      );

  public static List<String> courseSlots =
      Arrays.asList(
          "welcome:CourseName",
          "welcome:CourseCertificate",
          "welcome:CourseStatus",
          "welcome:CourseSchool",
          "welcome:CourseSchoolType",
          "welcome:CourseYear",
          "welcome:CourseDuration",
          "welcome:CourseGrade"
      );

  public static List<String> languageSlotsFull =
      Arrays.asList(
          "welcome:IntroductionLanguage",
          "welcome:IncludeAdditionalLanguage",
          "welcome:AdditionalLanguageName",
          "welcome:AdditionalLanguageProcedure",
          "welcome:AdditionalLanguageLevel",
          "welcome:AdditionalLanguageCertificate",
//          "welcome:AdditionalLanguageDegree",
          "welcome:AdditionalLanguageOther",
          "welcome:AdditionalLanguageCourse",
          "welcome:AdditionalLanguageCourseCertificate",
          "welcome:AdditionalLanguageCourseStatus",
          "welcome:AdditionalLanguageCourseSchool",
          "welcome:AdditionalLanguageCourseSchoolType",
          "welcome:AdditionalLanguageCourseYear",
          "welcome:AdditionalLanguageCourseName",
          "welcome:AdditionalLanguageCourseDuration",
          "welcome:AdditionalLanguageCourseGrade",
          "welcome:AdditionalLanguageCountry",
          "welcome:AdditionalLanguageCountryInclude",
          "welcome:AdditionalLanguageCountryName",
          "welcome:AdditionalLanguageCountryDuration",
          "welcome:AdditionalLanguage"
      );

  public static List<String> addLanguageSlots =
      Arrays.asList(
          "welcome:AdditionalLanguageName",
          "welcome:AdditionalLanguageLevel",
          "welcome:AdditionalLanguageCertificate",
          "welcome:AdditionalLanguageOther",
          "welcome:AdditionalLanguageCourse",
          "welcome:AdditionalLanguageCourseCertificate",
          "welcome:AdditionalLanguageCourseStatus",
          "welcome:AdditionalLanguageCourseSchool",
          "welcome:AdditionalLanguageCourseSchoolType",
          "welcome:AdditionalLanguageCourseYear",
          "welcome:AdditionalLanguageCourseName",
          "welcome:AdditionalLanguageCourseDuration",
          "welcome:AdditionalLanguageCourseGrade",
          "welcome:AdditionalLanguageCountry",
          "welcome:AdditionalLanguageCountryInclude",
          "welcome:AdditionalLanguageCountryName",
          "welcome:AdditionalLanguageCountryDuration",
          "welcome:AdditionalLanguage"
      );

  public static List<String> languageSlots =
      Arrays.asList(
          "welcome:AdditionalLanguageName",
          "welcome:AdditionalLanguageLevel",
          "welcome:AdditionalLanguageCertificate",
//          "welcome:AdditionalLanguageDegree",
          "welcome:AdditionalLanguageOther",
          "welcome:AdditionalLanguageCourse",
          "welcome:AdditionalLanguageCourseCertificate",
          "welcome:AdditionalLanguageCourseStatus",
          "welcome:AdditionalLanguageCourseSchool",
          "welcome:AdditionalLanguageCourseSchoolType",
          "welcome:AdditionalLanguageCourseYear",
          "welcome:AdditionalLanguageCourseName",
          "welcome:AdditionalLanguageCourseDuration",
          "welcome:AdditionalLanguageCourseGrade",
          "welcome:AdditionalLanguageCountry",
          "welcome:AdditionalLanguageCountryInclude",
          "welcome:AdditionalLanguageCountryName",
          "welcome:AdditionalLanguageCountryDuration"
      );

  public static List<String> employmentSlotsFull =
      Arrays.asList(
          "welcome:IntroductionEmployment",
          "welcome:EmploymentStatus",
          "welcome:Occupation",
          "welcome:EmployerName",
          "welcome:EmployerAddress",
          "welcome:StartingDate",
          "welcome:MainActivities",
          "welcome:PreviousEmploymentStatus",
          "welcome:PreviousOccupation",
          "welcome:PreviousEmployerName",
          "welcome:PreviousEmployerAddress",
          "welcome:PreviousStartingDate",
          "welcome:PreviousEndDate",
          "welcome:PreviousNumberHours",
          "welcome:PreviousMainActivities",
          "welcome:AdditionalEmployment"
      );

  public static List<String> addEmploymentSlots =
      Arrays.asList(
          "welcome:PreviousEmploymentStatus",
          "welcome:PreviousOccupation",
          "welcome:PreviousEmployerName",
          "welcome:PreviousEmployerAddress",
          "welcome:PreviousStartingDate",
          "welcome:PreviousEndDate",
          "welcome:PreviousNumberHours",
          "welcome:PreviousMainActivities",
          "welcome:AdditionalEmployment"
      );

  public static List<String> currentEmploymentSlots =
      Arrays.asList(
          "welcome:CurrentlyEmployed", /* Works as a flag */
          "welcome:Occupation",
          "welcome:EmployerName",
          "welcome:EmployerAddress",
          "welcome:StartingDate",
          "welcome:EndDate", /* This is created with value "Current" */
          "welcome:MainActivities"
      );

  public static List<String> pastEmploymentSlots =
      Arrays.asList(
          "welcome:PreviousOccupation",
          "welcome:PreviousEmployerName",
          "welcome:PreviousEmployerAddress",
          "welcome:PreviousStartingDate",
          "welcome:PreviousEndDate",
          "welcome:PreviousNumberHours",
          "welcome:PreviousMainActivities"
      );

  /* List of slots with yes/no answers that are Boolean */
  public static List<String> booleanSlots =
      Arrays.asList(
          "confirmCommunication",
          "obtainInterest",
          "obtainInterestRegistration",
          "obtainConfirmation",
          "confirmFirstSurname",
          "obtainConfirmationNGOSupport",
          "obtainConfirmationSkypeUser",
          "obtainConfirmationContactPRAKSIS",
          "obtainConfirmationSkypeProcess",
          "obtainConfirmationInternetConnection",
          "obtainConfirmationNormalClosing",
          "obtainConfirmationContactDetails",
          "obtainConfirmationDetailsAppointment",
          "obtainReadiness",
          "obtainDetailsKnown",
          "obtainConfirmationRequiredInfo",
          "obtainConfirmationSurnameStep",
          "obtainConfirmationNameStep",
          "obtainConfirmationBirthdateStep",
          "obtainConfirmationTelephoneStep",
          "obtainConfirmationCaptchaStep",
          "obtainVisualizationInterest",
          "obtainConfirmationDisclaimer",
          "obtainConfirmationUsage",
          "obtainPrintedInterest",
          "obtainConfirmationLogin",
          "obtainConfirmationLanguage",
          "obtainCheckingConfirmation",
          "obtainCallbackConfirmation",
          "obtainAppointmentDocumentsConfirmation",
          "obtainFirstContactPRAKSIS",
          "obtainPremisesVisitConfirmation",
          "obtainAforetimeReminderConfirmation",
          "obtainIncludeFullAddress",
          "obtainPersonalInfoConfirmation",
          "obtainRegistrationInterest",
          "obtainOverviewInterest",
          "obtainAsylumClaimRegistration",
          "obtainContactNGOSimulationConfirmation",
          "obtainAdditionalLanguageConfirmation", /* S7 additional language */
          "obtainAdditionalLanguageOther", /* S7 language course or country */
          "obtainAdditionalLanguageCourse",
          "obtainAdditionalLanguageCountry",
          "obtainAdditionalLanguageCountryInclude",
          "obtainEmploymentStatus", /* S7 Current Employment Status */
          "obtainPreviousEmploymentStatus", /* S7 Previous Employment Status */
          "obtainPreviousNumberHoursConfirmation", /* S7 Hours confirmation */
          "obtainIncludeCarInfo",
          "obtainSatisfaction", /* FAQ related slots */
          "obtainFollowUpSatisfaction",
          "obtainFAQSatisfaction",
          "obtainContinueInterest",
          "obtainCVCompletionConfirmation",
          "obtainDocumentsAvailability", /* S8 related skills */
          "obtainAvailableTranslation",
          "obtainUMasterRecogInfoConfirmation",
          "obtainSARUInfoConfirmation",
          "obtainTranslationInfoConfirmation",
          "obtainAgreementProceed",
          "obtainRequirementsSatisfied",
          "obtainAppointmentContactConfirmation",
          "obtainExistingCV",
          "obtainCVAssistance",
          "obtainCVAssistanceConfirmation",
          "obtainNewCVAssistance",
          "obtainAgreementProceed",
          "obtainEducationDepartmentConfirmation",
          "obtainLeavingCertificate",
          "obtainOriginalCopy",
          "obtainCertificateTranslation",
          "obtainCertifiedCopyNeedConfirmation",
          "obtainUrgency",
          "obtainContinue",
          "obtainGettingCertificate",
          "obtainUsefulness",
          "obtainAvailableCertifiedCopy",
          "obtainNGOAssistance",
          "obtainGettingOriginalCopy",
          "obtainLegislationConfirmation", // S5 related slots
          "obtainSSNumberAvailable",
          "obtainPAAYPAConfirmation",
          "obtainInfoSufficient",
          "obtainRegistrationInterest",
          "obtainContinueScenario"
      );

  public static List<String> dateSlots =
      Arrays.asList(
          "obtainBirthDay",
          "obtainBirthMonth",
          "obtainBirthYear",
          "obtainYearArrival",
          "obtainYearArrivalRegion",
          "obtainRegistrationYear",
          "obtainYearCompletionCatalan",
          "obtainYearCompletionSpanish",
          "obtainYearCompletionLabourMarket",
          "obtainYearCompletionCatalanSociety",
          "obtainMoreYear",
          "obtainMoreEducationalCourseYear",
          "obtainAdditionalLanguageCourseYear",
          "obtainAdditionalLanguageCourseDuration",
          "obtainStartingDate", /* S7 employment starting date */
          "obtainPreviousStartingDate", /* S7 employment prev. starting date */
          "obtainPreviousEndDate" /* S7 employment prev. end date */
      );

  public static List<String> durationSlots =
      Arrays.asList(
          "obtainMoreEducationalCourseDuration",
          "obtainAdditionalLanguageCourseDuration",
          "obtainAdditionalLanguageCountryDuration"
      );

  public static List<String> personSlots =
      Arrays.asList(
          "obtainName",
          "obtainNameAppointment",
          "obtainFirstSurname",
          "obtainSecondSurname",
          "confirmFirstSurname"
      );

  public static List<String> negationSlots =
      Arrays.asList(
          "obtainBuildingName",
          "obtainEntrance",
          "obtainDoorNumber",
          "obtainApartmentNumber",
          "obtainSecondSurname",
          "obtainLandline",
          "obtainMobilePhone",
          "obtainCardNumber",
          "obtainAsylumPreRegistrationNumber",
          "obtainPhoneNumber",
          "obtainPhoneNumberAppointment",
          "obtainMoreGrade"
      );

  public static List<String> negationRelSlots =
      Arrays.asList(
          "obtainMoreDegreeTitle"
      );

  public static List<String> courseNames =
      Arrays.asList(
          "obtainCourseNameCatalan",
          "obtainCourseNameSpanish",
          "obtainCourseNameLabourMarket",
          "obtainCourseNameCatalanSociety"
      );

  /* List of slots with obtainStatus */
  public static List<String> statusSlot =
      Arrays.asList(
          "obtainStatus"
      );

  /* List of slots with obtainRequest */
  public static List<String> requestSlot =
      Arrays.asList(
          "obtainRequest"
      );

  /* List of slots with yes/no answers other than Boolean */
  public static List<String> confirmSlots =
      Arrays.asList(
          "obtainStatus",
          "confirmLanguage",
          "obtainNotificationPreferences",
          "obtainSMSNotifications",
          "obtainEMailNotifications",
          "obtainLearningHandicap",
          "obtainIlliteracy",
          "obtainKnowledgeCatalan",
          "obtainCoursesCatalan",
          "obtainKnowledgeSpanish",
          "obtainCoursesSpanish",
          "obtainKnowledgeLabour",
          "obtainCoursesLabour",
          "obtainKnowledgeSociety",
          "obtainCoursesSociety",
          "obtainFirstApplication",
          "obtainKnowledgeSkypeCreation",
          "obtainInternetConnection",
          "obtainCertificateCatalan",
          "obtainCertificateSpanish",
          "obtainCertificateLabourMarket",
          "obtainCertificateCatalanSociety",
          "obtainRegistration",
          "obtainApplicationKnowledge",
          "obtainMoreDegreeCertificate",
          "obtainSuggestOtherEducation",
          "obtainMoreEducationalCourseCertificate",
          "obtainAdditionalLanguageCertificate",
          "obtainAdditionalLanguageCourseCertificate",
          "obtainDrivingLicense",
          "obtainCar"
      );

  /* Check if it is a number related entity */
  public static List<String> numSlots =
      Arrays.asList(
          "obtainResidenceAddressNumber",
          "obtainLandline",
          "obtainMobilePhone",
          "obtainPhoneNumber",
          "obtainStreetNumber",
          "obtainFloorNumber",
          "obtainDoorNumber",
          "obtainApartmentNumber",
          "obtainPostCode",
          "obtainIDNumber",
          "obtainDurationCatalan",
          "obtainDurationSpanish",
          "obtainDurationLabourMarket",
          "obtainDurationCatalanSociety",
          "obtainBirthYear",
          "obtainBirthDay",
          "obtainBirthday",
          "obtainCardNumber",
          "obtainAsylumPreRegistrationNumber",
          "obtainPhoneAppointmentRejection",
          "obtainPhoneNumberAppointment",
          "obtainMoreNumberClasses",
          "obtainMoreGrade",
          "obtainMoreEducationalCourseGrade",
          "obtainPreviousNumberHours", /* it can also be in the duration list */
          "obtainNumberFinishedClasses" /* S8 related slot */
      );

  /* Check if it is a location related entity */
  public static List<String> locSlots =
      Arrays.asList(
          "obtainResidenceAddressCity",
          "obtainResidenceAddressStreet",
          "obtainIDCountry",
          "obtainCountryOfBirth",
          "obtainCityOfBirth",
          "obtainNationality",
          "obtainCity",
          "obtainProvince",
          "obtainMunicipality",
          "obtainStreetName",
          "obtainAdditionalLanguageCountryName", /* S7 */
          "obtainEmployerAddress", /* Employment address */
          "obtainPreviousEmployerAddress" /* Previous Employment address */
      );

  public static List<String> langSlots =
      Arrays.asList(
          "obtainMotherTongue",
          "obtainAdditionalLanguageName",
          "confirmLanguage"
      );

  /* Possible types of positive(ish) responses */
  public static List<String> positiveResponses =
      Arrays.asList(
          "agree/accept",
          "yes answers",
          "response acknowledgement",
          "affirmative non-yes answers",
          "acknowledge (backchannel)",
          "action-directive",
          "conventional-closing",
          "appreciation"
      );

  /* Possible types of negative(ish) responses */
  public static List<String> negativeResponses =
      Arrays.asList(
          "no answers",
          "reject",
          "negative non-no answers",
          "dispreferred answers"
      );

  /* Possible types of unclear responses */
  public static List<String> unclearResponses =
      Arrays.asList(
          "hedge",
          "signal-non-understanding",
          "apology");

  /* Possible types of question responses */
  public static List<String> questionResponses =
      Arrays.asList(
          "yes-no-question",
          "wh-question",
          "declarative yes-no-question",
          "backchannel in question form",
          "or-clause",
          "declarative wh-question",
          "rhetorical-question"
      );

  /* informModule */
  public static List<String> informModule =
      Arrays.asList(
          "informLanguageModule",
          "informLabourModule",
          "informSocietyModule"
      );

  /* informModuleAddress */
  public static List<String> informModuleAddress =
      Arrays.asList(
          "informLanguageModuleAddress",
          "informLabourModuleAddress",
          "informSocietyModuleAddress"
      );

  /* informModuleHours */
  public static List<String> informModuleHours =
      Arrays.asList(
          "informLanguageModuleHours",
          "informLabourModuleHours",
          "informSocietyModuleHours"
      );

  /* informSkype */
  public static List<String> informSkype =
      Arrays.asList(
          "informTimeSlot",
          "informSkypeID"
      );

  /* informPraksis */
  public static List<String> informPraksis =
      Arrays.asList(
          "informTelephoneNumber",
          "informEmail",
          "informAddress"
      );

  /* informRegistration */
  public static List<String> informRegistration =
      Arrays.asList(
          "informOfficeAddress",
          "informOfficeHours"
      );

  /* Group of slots related to residence address */
  public static List<String> residenceInfo =
      Arrays.asList(
          "obtainResidenceAddressCity",
          "obtainResidenceAddressStreet",
          "obtainResidenceAddressNumber"
      );

  public static List<String> multipleChoice =
      Arrays.asList(
          "Passport",
          "DNI",
          "NIE",
          "Male",
          "Female",
          "Non-binary",
          "Prefer not to say",
          "Other",
          "Street",
          "Avenue",
          "Park",
          "Apartment",
          "Studio",
          "Family house",
          "Block",
          "Highrise",
          "Loft",
          "Single",
          "Married",
          "Divorced",
          "Widow",
          "Widower",
          "Carrer",
          "Avinguda",
          "Placa",
          "House"
      );

  public static List<String> appointmentConcern =
      Arrays.asList(
          "I received a letter from you and (I think I have to contact you) I want to make an appointment with you.",
          "I need your help please unfortunately, my fixed-term contract will not be extended and I haven’t found another job yet."
      );

  public static List<String> multipleSlots =
      Arrays.asList(
          "obtainIDType",
          "obtainGender",
          "obtainStreetType",
          "obtainBuildingType",
          "obtainMaritalStatus"
      );

  public static List<String> months =
      Arrays.asList(
          "January",
          "February",
          "March",
          "April",
          "May",
          "June",
          "July",
          "August",
          "September",
          "October",
          "November",
          "December"
      );

  public static List<String> ordinal =
      Arrays.asList(
          "first",
          "second",
          "third",
          "fourth",
          "fifth",
          "sixth",
          "seventh",
          "eighth",
          "ninth",
          "tenth",
          "eleventh",
          "twelfth",
          "thirteenth",
          "fourteenth",
          "fifteenth",
          "sixteenth",
          "seventeenth",
          "eighteenth",
          "nineteenth",
          "twentieth",
          "twenty first",
          "twenty second",
          "twenty third",
          "twenty fourth",
          "twenty fifth",
          "twenty sixth",
          "twenty seventh",
          "twenty eighth",
          "twenty ninth",
          "thirtieth",
          "thirty first"
      );

  public static List<String> previousResidence =
      Arrays.asList(
          "obtainPreviousResidenceCatalonia",
          "obtainPreviousResidenceSpain",
          "obtainPreviousResidenceOther",
          "obtainAsylumClaim",
          "obtainNationality"
      );

  public static List<String> orgSlots =
      Arrays.asList(
          "obtainCourseInstitutionCatalan",
          "obtainCourseInstitutionSpanish",
          "obtainCourseInstitutionLabourMarket",
          "obtainCourseInstitutionCatalanSociety",
          "obtainBuildingName",
          "obtainMoreSchool",
          "obtainMoreEducationalCourseSchool",
          "obtainAdditionalLanguageCourseSchool",
          "obtainEmployerName", /* Employment Slots */
          "obtainPreviousEmployerName"
      );

  public static List<String> appForm =
      Arrays.asList(
          "welcome:AccountStatus",
          "welcome:ActivityParticipation",
          "welcome:AddictionIssues",
          "welcome:Administrator",
          "welcome:Age",
          "welcome:ApartmentNumber",
          "welcome:ArrivalDate",
          "welcome:AsylumApplication",
          "welcome:AsylumClaim",
          "welcome:AsylumPreRegistrationNumber",
          "welcome:AvatarConfig",
          "welcome:Birthday",
          "welcome:BuildingName",
          "welcome:BuildingNumber",
          "welcome:BuildingType",
          "welcome:CertificateCatalan",
          "welcome:CertificateCatalanSociety",
          "welcome:CertificateLabourMarket",
          "welcome:CertificateSpanish",
          "welcome:City",
          "welcome:CityOfBirth",
          "welcome:CompletedModules",
          "welcome:CompletionYearCatalan",
          "welcome:CompletionYearCatalanSociety",
          "welcome:CompletionYearLabourMarket",
          "welcome:CompletionYearSpanish",
          "welcome:Country",
          "welcome:CountryCode",
          "welcome:CountryOfBirth",
          "welcome:CountryOfBirthCode",
          "welcome:CountryOfOrigin",
          "welcome:CountryOfOriginCode",
          "welcome:CountryOfOriginProfession",
          "welcome:CourseInstitutionCatalan",
          "welcome:CourseInstitutionCatalanSociety",
          "welcome:CourseInstitutionLabourMarket",
          "welcome:CourseInstitutionSpanish",
          "welcome:CourseNameCatalan",
          "welcome:CourseNameCatalanSociety",
          "welcome:CourseNameLabourMarket",
          "welcome:CourseNameSpanish",
          "welcome:CoursesCatalan",
          "welcome:CoursesLabour",
          "welcome:CoursesSociety",
          "welcome:CoursesSpanish",
          "welcome:DigitalSkills",
          "welcome:DoorNumber",
          "welcome:DurationCatalan",
          "welcome:DurationCatalanSociety",
          "welcome:DurationLabourMarket",
          "welcome:DurationOfStay",
          "welcome:DurationSpanish",
          "welcome:EducationLevel",
          "welcome:Email",
          "welcome:EmailNotifications",
          "welcome:EmploymentStatus",
          "welcome:Entrance",
          "welcome:Ethnicity",
          "welcome:FamilyMembers",
          "welcome:FirstSurname",
          "welcome:FloorNumber",
          "welcome:Gender",
          "welcome:HealthIssues",
          "welcome:Hobbies",
          "welcome:HostCountry",
          "welcome:HostCountryCode",
          "welcome:HostCountryProfession",
          "welcome:HostLangLevel",
          "welcome:IDCountry",
          "welcome:IDCountryCode",
          "welcome:IDNumber",
          "welcome:IDType",
          "welcome:Illiteracy",
          "welcome:ImmigratedAlone",
          "welcome:InitDataCollection",
          "welcome:KnowledgeCatalan",
          "welcome:KnowledgeLabour",
          "welcome:KnowledgeSociety",
          "welcome:KnowledgeSpanish",
          "welcome:Landline",
          "welcome:Language",
          "welcome:LanguageCode",
          "welcome:LccGenderPreference",
          "welcome:LccGenderPrefWeight",
          "welcome:LccNationPreference",
          "welcome:LccNationPrefWeight",
          "welcome:LearningHandicap",
          "welcome:LegalStatus",
          "welcome:MaritalStatus",
          "welcome:MobilePhone",
          "welcome:Municipality",
          "welcome:Name",
          "welcome:Nationality",
          "welcome:NationalityCode",
          "welcome:NativeLanguageName",
          "welcome:NativeLanguageCode",
          "welcome:NotificationPreferences",
          "welcome:NumberAccompaniedChildren",
//          "welcome:OtherLanguage",
//          "welcome:OtherLanguageCode",
          "welcome:PendingModules",
          "welcome:PostalAddress",
          "welcome:PostCode",
          "welcome:PreferredLanguage",
          "welcome:PreferredLanguageCode",
          "welcome:PreviousResidenceCatalonia",
          "welcome:PreviousResidenceOther",
          "welcome:PreviousResidenceOtherCode",
          "welcome:PreviousResidenceSpain",
          "welcome:PrevResidenceBuildingNumber",
          "welcome:PrevResidenceCity",
          "welcome:PrevResidenceCountry",
          "welcome:PrevResidencePostCode",
          "welcome:PrevResidenceProvince",
          "welcome:PrevResidenceStreetName",
          "welcome:Province",
          "welcome:RegistrationDate",
          "welcome:RegistrationStatus",
          "welcome:RegistrationYear",
          "welcome:ResidenceStatus",
          "welcome:Route",
          "welcome:RouteStartDate",
          "welcome:SecondSurname",
          "welcome:SMSNotifications",
          "welcome:StreetName",
          "welcome:StreetNumber",
          "welcome:StreetType",
          "welcome:TotalNumberChildren",
          "welcome:UserId",
          "welcome:Username",
          "welcome:WorkExperienceCountryOfOrigin",
          "welcome:WorkPermit",
          "welcome:YearArrivalCity",
          "welcome:YearArrivalCountry",
          "welcome:YearArrivalRegion"
      );

  public static List<String> dow =
      Arrays.asList(
          "monday",
          "tuesday",
          "wednesday",
          "thursday",
          "friday"
      );

  public static List<String> daytime =
      Arrays.asList(
          "morning",
          "afternoon"
      );

  public static List<String> allowsString =
      Arrays.asList(
          "obtainMoreDegreeTitle", /* Education section */
          "obtainMoreEducationalCourseSchoolType",
          "obtainMoreEducationalCourseName",
          //"obtainMoreSchool",
          //"obtainMoreSchoolCompleted",
          "obtainCurrentOccupation", /* Employment section */
          "obtainPreviousOccupation",
          "obtainMainActivities",
          "obtainPreviousMainActivities",
          "obtainAdditionalLanguageLevel", /* Language section */
          "obtainAdditionalLanguageCourseName",
          "obtainAdditionalLanguageCourseSchoolType",
          "obtainAdditionalLanguageCourseGrade"
      );

  public static List<String> statusSlots =
      Arrays.asList(
          "obtainMoreSchoolCompleted",
          "obtainMoreEducationalCourseStatus",
          "obtainAdditionalLanguageCourseStatus"
      );

  public static List<String> S7LanguageSlots =
      Arrays.asList(
          "\"AdditionalLanguageLevel\"",
          "\"AdditionalLanguageOther\"",
          "\"AdditionalLanguageCourse\"",
          "\"AdditionalLanguageCountry\"",
          "\"AdditionalLanguageCountryInclude\""
      );

  public static List<String> S7CountrySlots =
      Arrays.asList(
          "\"AdditionalLanguageCountryDuration\""
      );

  public static List<String> chcPreferencesObjects =
      Arrays.asList(
          "speaksLanguage",
          "ChcAgePreference",
          "ChcFamilyPreference",
          "ChcLocationPreference",
          "ChcRentPeriodPreference",
          "ChcShareWithPreference"
      );

  public static List<String> skipSlots =
      Arrays.asList(
          "obtainIntroductionEducation",
          "obtainSuggestOtherEducation",
          "informLanguagesSection",
          "informIntroductionEmployment",
          "informOtherSectionIntro"
      );

  public static List<String> cvSections =
      Arrays.asList(
          "CVPurpose",
          "PersonalInfo",
          "EducationInformation",
          "OtherEducationInformation",
          "LanguageInformation",
          "EmploymentInformation",
          "SkillInformation",
          "OtherInformation",
          "CreateCVDocument"
      );
}
