-- after creating new Sales. Cities...

USE [WideWorldImporters]
GO
/*
INSERT INTO [Sales].[Cities] (
           [CityName]
           ,[StateProvinceID]
           ,[Location]
           ,[LatestRecordedPopulation]
           ,[LastEditedBy])
SELECT [CityName]
        ,[StateProvinceID]
        ,[Location]
        ,[LatestRecordedPopulation]
        ,[LastEditedBy] from Application.Cities
 */