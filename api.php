<?php
require_once "config.php";

$response = [ "success" => false ];

$dbc = new PDO("mysql:host=" . Config::$DB_HOST . ";dbname=" . Config::$DB_NAME, Config::$DB_USERNAME, Config::$DB_PASSWORD);

if (isset($_REQUEST["s"]))
{
	switch ($_REQUEST["s"])
	{
		case "vars":
		
			$sql = $dbc->prepare("SELECT program, position FROM oplog GROUP BY program, position");
			if ($sql->execute())
			{
				$response["programs"] = $sql->fetchAll(PDO::FETCH_ASSOC);
				
				$sql = $dbc->prepare("SELECT column_name, column_label FROM opvar");
				if ($sql->execute())
				{
					$response["data"] = $sql->fetchAll(PDO::FETCH_ASSOC);
					$response["success"] = true;
				}
			}
		
			break;
			
		case "values":
		
			$sql = $dbc->prepare("SELECT id, " . preg_replace("/(?!(\w|\d|\,))/i", "", $_REQUEST["vars"]) . " FROM oplog WHERE program = :f1 AND position = :f2 ORDER BY id ASC");
				$sql->bindValue(":f1", $_REQUEST["program"]);
				$sql->bindValue(":f2", $_REQUEST["position"]);
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