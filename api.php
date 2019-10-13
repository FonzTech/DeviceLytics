<?php
require_once "config.php";

$response = [ "success" => false ];

$dbc = new PDO("mysql:host=" . Config::$DB_HOST . ";dbname=" . Config::$DB_NAME, Config::$DB_USERNAME, Config::$DB_PASSWORD);

if (isset($_REQUEST["s"]))
{
	switch ($_REQUEST["s"])
	{
		case "vars":
		
			$sql = $dbc->prepare("SELECT column_name, column_label FROM opvar");
			if ($sql->execute())
			{
				$response["vars"] = $sql->fetchAll(PDO::FETCH_ASSOC);
				$response["success"] = true;
			}
		
			break;
	}
}

$dbc = null;

die(json_encode($response));
?>