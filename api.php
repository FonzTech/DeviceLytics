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
				$response["data"] = $sql->fetchAll(PDO::FETCH_ASSOC);
				$response["success"] = true;
			}
		
			break;
			
		case "values":
		
			$sql = $dbc->prepare("SELECT id, " . preg_replace("/(?!(\w|\d|\,))/i", "", $_REQUEST["vars"]) . " FROM oplog ORDER BY id ASC");
			if ($sql->execute())
			{
				$response["data"] = $sql->fetchAll(PDO::FETCH_ASSOC);
				$response["success"] = true;
			}
		
			break;
	}
}

$dbc = null;

die(json_encode($response));
?>