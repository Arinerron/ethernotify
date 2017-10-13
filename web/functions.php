<?php
    include_once 'config.php';

    define('YEAR', 'YEAR');
    define('MONTH', 'MONTH');
    define('WEEK', 'WEEK');
    define('DAY', 'DAY');
    define('HOUR', 'HOUR');
    define('MINUTE', 'MINUTE');

    /* returns a new mysqli object */
    function getConnection() {
        return new mysqli(DATABASE_HOST, DATABASE_USER, DATABASE_PASSWORD, DATABASE_NAME);
    }

    /* gets the current price of a currency on coinbase */
    function getPrice($currency) {
        $contents = json_decode(file_get_contents("https://api.coinbase.com/v2/prices/" . strtoupper($currency) . "-USD/spot"), true);
        return floatval($contents['data']['amount']);
    }

    /* get the prices of all currencies */
    function getPrices() {
        return array('BTC'=>getPrice('BTC'), 'ETH'=>getPrice('ETH'), 'LTC'=>getPrice('LTC'));
    }

    /* updates the history table */
    function update() {
        $prices = getPrices();

        addHistory('BTC', $prices['BTC']);
        addHistory('ETH', $prices['ETH']);
        addHistory('LTC', $prices['LTC']);
    }

    /* adds a row to the history table */
    function addHistory($currency, $price, $exchange='Coinbase') {
        $mysqli = getConnection();
        $stmt = $mysqli->prepare("INSERT INTO `history` (`time`, `exchange`, `currency`, `price`) VALUES (NULL, ?, ?, ?)");
        $stmt->bind_param('sss', $exchange, $currency, $price);
        $stmt->execute();
    }

    /* past hour: SELECT * FROM `history` WHERE time > NOW() - INTERVAL 59.5 MINUTE AND time < NOW() - INTERVAL 60.5 MINUTE */
    /* past hour: SELECT * FROM `history` WHERE DATE_FORMAT(time, '%Y-%m-%d %H:%i:00') = DATE_FORMAT(NOW() - INTERVAL 60 MINUTE, '%Y-%m-%d %H:%i:00') */
    /* interval hour: SELECT * FROM `history` WHERE mod(abs(minute(CURRENT_TIMESTAMP) - minute(DATE_FORMAT(`time`, '%Y-%m-%d %H:%i:00'))), 60) = 0*/

    /* gets prices from x interval ago */
    function getHistory($time, $interval='HOUR', $limit=1000) {
        // get a row from an hour ago: getHistory(1, HOUR)
        // get a row from 5 minutes ago: getHistory(5, MINUTE)
        // get a row from 20 years ago: getHistory(20, YEAR)

        // NOTE: Week may not work!

        $arr = array();

        $mysqli = getConnection();

        $time = preg_replace("/[^0-9 ]/", '', $time);
            $limit = preg_replace("/[^0-9 ]/", '', $limit);
        $interval = strtoupper(preg_replace("/[^A-Za-z0-9 ]/", '', $interval));
        $lowerinterval = strtolower($interval);

        $payload = '%Y-%m-%d %H:%i:00';
        $after = '';

        // temp solution TODO: improve
        if($interval === 'HOUR') {
            $payload = '%Y-%m-%d %H:00:00';
            $after = ' AND minute(`time`) = minute(CURRENT_TIMESTAMP)';
        } else if($interval === 'DAY') {
            $payload = '%Y-%m-%d 00:00:00';
            $after = ' AND minute(`time`) = minute(CURRENT_TIMESTAMP) AND hour(`time`) = hour(CURRENT_TIMESTAMP)';
        } else if($interval === 'WEEK') {
            $payload = '%Y-%m-%d 00:00:00'; // this is why week may not work. %d isn't rounded to nearest 07
            $after = ' AND minute(`time`) = minute(CURRENT_TIMESTAMP) AND hour(`time`) = hour(CURRENT_TIMESTAMP) AND day(`time`) / 7 = day(CURRENT_TIMESTAMP) / 7';
        } else if($interval === 'MONTH') {
            $payload = '%Y-%m-00 00:00:00';
            $after = ' AND minute(`time`) = minute(CURRENT_TIMESTAMP) AND hour(`time`) = hour(CURRENT_TIMESTAMP) AND day(`time`) = day(CURRENT_TIMESTAMP)';
        } else if($interval === 'YEAR') {
            $payload = '%Y-00-00 00:00:00';
            $after = ' AND minute(`time`) = minute(CURRENT_TIMESTAMP) AND hour(`time`) = hour(CURRENT_TIMESTAMP) AND day(`time`) = day(CURRENT_TIMESTAMP) AND month(`time`) = month(CURRENT_TIMESTAMP)';
        }

        $stmt = $mysqli->prepare("SELECT * FROM `history` WHERE mod(abs(" . $lowerinterval ."(CURRENT_TIMESTAMP) - " . $lowerinterval . "(DATE_FORMAT(`time`, '" . $payload . "'))), " . $time . ") = 0" . $after . " LIMIT " . $limit);
        $stmt->execute();

        $stmt->store_result();
        $stmt->bind_result($time, $exchange, $currency, $price);
        while ($stmt->fetch()) {
            $arr[] = array(
                "time"=>$time,
                "exchange"=>$exchange,
                "currency"=>$currency,
                "price"=>$price
            );
        }

        return $arr;
    }

    function getHistoryAtTime($time, $unit='HOUR') {
        $unit = strtoupper($unit);
        $array = array('HOUR'=>60, 'MINUTE'=>1, 'DAY'=>60*24, 'WEEK'=>60*24*7, 'MONTH'=>60*24*30, 'YEAR'=>60*24*265);
        $arr = array();

        $time = intval(preg_replace("/[^0-9 ]/", '', $time)) * $array[$unit];

        $stmt = $mysqli->prepare("SELECT * FROM `history` WHERE abs((CURRENT_TIMESTAMP) - (DATE_FORMAT(`time`, '%Y-%m-%d %H:%i:00'))), " . $time . ") = 0" . $after . " LIMIT 3");
        $stmt->execute();

        $stmt->store_result();
        $stmt->bind_result($time, $exchange, $currency, $price);
        while ($stmt->fetch()) {
            $arr[] = array(
                "time"=>$time,
                "exchange"=>$exchange,
                "currency"=>$currency,
                "price"=>$price
            );
        }

        return $arr;
    }

    /* converts two prices to percent change */
    function getPercent($from, $to) {
        return (100 * ($to - $from) / $from);
    }


    /* Dumps the data in an array in the format requested */
    function dump($array) {
        if(isset($_REQUEST['format'])) {
            $type = $_REQUEST['format'];
            if($type == 'dump') {
                header("Content-Type: text/plain");
                print_r($array); // phpdump
                die();
            } else if($type == 'xml') {
                header("Content-Type: application/xml");
                die(toXML($array));
            }
        }

        header("Content-Type: application/json");
        die(json_encode($array)); // json
    }

    /* converts array to xml */
    function toXML($array) {
        $xml = new SimpleXMLElement("<?xml version=\"1.0\"?><root></root>");

        foreach($array as $key => $value) {
            if(is_array($value)) {
                $key = is_numeric($key) ? "item$key" : $key;
                $subnode = $xml->addChild("$key");
                array_to_xml($value, $subnode);
            }
            else {
                $key = is_numeric($key) ? "item$key" : $key;
                $xml->addChild("$key","$value");
            }
        }

        return $xml->asXML();
    }
?>
