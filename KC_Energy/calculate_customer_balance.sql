CREATE DEFINER=`root`@`localhost` PROCEDURE `calculate_customer_balance`(IN SelectedAccount int)
BEGIN
DECLARE finished BOOLEAN DEFAULT false;
DECLARE mID int;
DECLARE totalSum Decimal(14,2) DEFAULT 0;

DECLARE MeterCursor CURSOR FOR 
	SELECT MeterID FROM meter WHERE custID = SelectedAccount;
DECLARE CONTINUE HANDLER 
        FOR NOT FOUND SET finished = true;
OPEN MeterCursor;

CalcMeterBalances: LOOP
	FETCH MeterCursor INTO mID;
    IF finished = true THEN 
		LEAVE CalcMeterBalances;
	END IF;
    CALL Calculate_Meter_Balance(mID);
END LOOP CalcMeterBalances;

SELECT SUM(Balance) INTO totalSum FROM meter WHERE custID = SelectedAccount;
IF totalSum IS NULL THEN
	SET totalSum = 0;
END IF;
UPDATE customer SET OutstandingBalance = totalSum WHERE AccountNumber = SelectedAccount;

END