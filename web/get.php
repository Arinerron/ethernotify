<?php
include_once 'functions.php';

dump(getHistory(intval($_REQUEST['time']), $_REQUEST['unit'], intval($_REQUEST['limit'])));
?>
