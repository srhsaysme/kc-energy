CREATE DEFINER=`root`@`localhost` PROCEDURE `calculate_meter_balance`(IN SelectedMeter int)
BEGIN
DECLARE totalSum Decimal(10,2) DEFAULT 0;
DECLARE totalUsage Decimal(7,2) DEFAULT 0;

SELECT SUM(Cost) INTO totalSum FROM charge WHERE (metID = SelectedMeter AND paid = false);
SELECT SUM(chargeAmount) INTO totalUsage FROM charge WHERE metID = SelectedMeter;
IF totalSum IS NULL THEN
	SET totalSum = 0;
END IF;
IF totalUsage IS NULL THEN
	SET totalUsage = 0;
END IF;
UPDATE meter SET Balance = totalSum, energyUsage = totalUsage WHERE meterID = SelectedMeter;
END