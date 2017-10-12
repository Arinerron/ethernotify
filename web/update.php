<?php
    include_once 'functions.php';

    $prices = getPrices();

    addHistory('BTC', $prices['BTC']);
    addHistory('ETH', $prices['ETH']);
    addHistory('LTC', $prices['LTC']);

    echo dump($prices);
?>
