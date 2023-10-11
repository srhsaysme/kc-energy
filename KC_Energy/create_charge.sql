CREATE DEFINER=`root`@`localhost` PROCEDURE `create_charge`(IN mID int, IN cAmount Decimal(5,2), IN paidStatus boolean)
BEGIN
DECLARE t Decimal(5,2);
DECLARE calcCost Decimal(8,2);

SELECT SUM(Tariff) INTO t FROM meter WHERE meterID = mID;
SET calcCost = t * cAmount;
INSERT INTO charge(metID, chargeAmount, AppliedTariff, Cost, ChargeDate, Paid) VALUES (mID, cAmount, t, calcCost, now(), paidStatus);
END