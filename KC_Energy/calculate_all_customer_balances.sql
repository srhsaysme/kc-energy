CREATE DEFINER=`root`@`localhost` PROCEDURE `calculate_all_customer_balances`()
BEGIN
DECLARE finished BOOLEAN DEFAULT false;
DECLARE accNum int;

DECLARE CustomerCursor CURSOR FOR 
	SELECT AccountNumber FROM customer;
DECLARE CONTINUE HANDLER 
        FOR NOT FOUND SET finished = true;
OPEN CustomerCursor;

CalcCustomerBalances: LOOP
	FETCH CustomerCursor INTO accNum;
    IF finished = true THEN 
		LEAVE CalcCustomerBalances;
	END IF;
    CALL Calculate_Customer_Balance(accNum);
END LOOP CalcCustomerBalances;
END