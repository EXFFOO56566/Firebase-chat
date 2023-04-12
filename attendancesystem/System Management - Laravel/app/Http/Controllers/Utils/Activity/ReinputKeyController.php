<?php

namespace App\Http\Controllers\Utils\Activity;

use App\Helpers\Helper;
use App\Http\Controllers\Controller;
use Auth;
use GuzzleHttp\Client;

class ReinputKeyController extends Controller
{
    /**
     * Create a new controller instance.
     *
     * @return void
     */
    public function __construct()
    {
        $this->middleware('auth');
    }

    /**
     * Reinput key.
     *
     * @param $code
     * @return void
     * @throws \GuzzleHttp\Exception\GuzzleException
     */
    public function index($code)
    {
        $params = ['query' => ['code' => $code, 'url' => request()->getHttpHost()]];
        $client = new Client();
        $url = 'https://connectwithdev.com/purchase/public/reinputKey';
        $res = $client->request('GET', $url, $params);

        if ($res->getStatusCode() == 200) {
            $array = json_decode($res->getBody(), true);

            if ($array['error'] == 'no') {
                $helCod = new Helper();
                $helCod->writeBcKey($code);
                $helCod->writeToFile($array['message']);
                echo "Success";
            } else {
                echo $array['message'];
            }
        }
    }
}
