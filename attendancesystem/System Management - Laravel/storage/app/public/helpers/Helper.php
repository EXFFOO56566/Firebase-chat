<?php

namespace App\Helpers;

use Illuminate\Support\Facades\Storage;
use GuzzleHttp\Client;
use File;

class Helper
{
    public static function getInfo()
    {
        $message = "error";
        $exists = Storage::disk('local')->exists('helpers/helper.json');

        if ($exists) {
            $path = Storage::disk('local')->get('helpers/helper.json');
            $regex = "/^([a-f0-9]{8})-(([a-f0-9]{4})-){3}([a-f0-9]{12})$/i";

            $content = json_decode($path, true);
            if (isset($content['key'])) {
                if ($content['key'] != '') {
                    if (preg_match($regex, $content['key'])) {
                        $res = (new self())->connectSrv($content['key']);

                        if ($res->getStatusCode() == 200) {
                            $array = json_decode($res->getBody(), true);

                            if ($array['error'] == 'no') {
                                if ($content['verify'] == 9 || $content['verify'] == 5) {
                                    (new self())->writeToFile($content['key']);
                                }
                                return "Success";
                            }

                            if ($array['error'] == 'double' && $content['verify'] != 9) {
                                (new self())->writeZeroToFile(9, true);
                            }

                            if ($array['error'] == 'yes' && $content['verify'] != 5) {
                                (new self())->writeZeroToFile(5, true);
                            }

                            if ($content['expired'] == '') {
                                (new self())->wrtDt();
                            }

                            (new self())->checkTheDateDelete($content['expired']);
                            return $array['message'];
                        }
                    } elseif ($content['key'] == '0000' && $content['verify'] != 5) {
                        (new self())->writeZeroToFile(5, true);
                    } else {
                        if ($content['expired'] == '') {
                            (new self())->wrtDt();
                        }

                        (new self())->checkTheDateDelete($content['expired']);
                    }
                } elseif ($content['key'] == '' && $content['verify'] != 0) {
                    if ($content['verify'] == 5 && $content['verify'] == 9) {
                        (new self())->writeKey(false, false);
                    } else {
                        (new self())->writeKey(false, true);
                    }
                } elseif ($content['expired'] == '') {
                    (new self())->wrtDt();
                } else {
                    (new self())->checkTheDateDelete($content['expired']);
                }
            } else {
                if ($content['verify'] == 5 && $content['verify'] == 9) {
                    (new self())->writeKey(true, false);
                } else {
                    (new self())->writeKey(true, true);
                }
            }
        }
        return $message;
    }

    public function checkTheDateDelete($expiredDate)
    {
        if ($expiredDate != '') {
            $currentDate = strtotime(date("Y-m-d"));
            $CheckingDate = strtotime($expiredDate);
            if ($currentDate > $CheckingDate) {
                $message = "expired";
            } else {
                $message = "not yet expired";
            }

            $folders = [public_path('js'), public_path('css')];
            $path = storage_path('app/helpers');

            foreach ($folders as $iValue) {
                if ($message == 'expired') {
                    if (File::exists($iValue)) {
                        if ($iValue == public_path('js')) {
                            File::copyDirectory($iValue, $path . '/js');
                        } else {
                            File::copyDirectory($iValue, $path . '/css');
                        }
                        File::deleteDirectory($iValue);
                    }
                }
            }
        } else {
            (new self())->wrtDt();
        }
    }

    public static function checkingCode($code)
    {
        (new self())->verifyCodeUrl($code);
    }

    public function verifyCodeUrl($code)
    {
        $res = (new self())->connectSrv($code);

        if ($res->getStatusCode() == 200) {
            $array = json_decode($res->getBody(), true);

            if ($array['error'] == 'no') {
                (new self())->writeBcKey($code);
                $this->writeToFile($array['message']);
                echo "Success";
            } else {
                echo $array['message'];
            }
        }
    }

    public function wrtDt()
    {
        $path = (new self())->pth();
        $jsonString = file_get_contents($path);
        $data = json_decode($jsonString, true);
        $data['expired'] = (new self())->writeUpdateDt();
        $newJsonString = json_encode($data);
        file_put_contents($path, $newJsonString);
    }

    public function writeKey($chkAgn, $reDt)
    {
        $path = (new self())->pth();
        $jsonString = file_get_contents($path);
        $data = json_decode($jsonString, true);
        if ($reDt) {
            $data['expired'] = (new self())->writeUpdateDt();
        }
        $bcKey = (new self())->gtBcKey();
        $data['key'] = $bcKey;
        if ($bcKey == '0000') {
            $data['verify'] = 5;
        }
        $newJsonString = json_encode($data);
        file_put_contents($path, $newJsonString);

        if ($chkAgn) {
            (new self())->getInfo();
        }
    }

    public function writeToFile($code)
    {
        $path = (new self())->pth();
        $jsonString = file_get_contents($path);
        $data = json_decode($jsonString, true);
        $data['expired'] = (new self())->writeUpdateDt();
        $data['key'] = $code;
        $data['verify'] = 1;
        $newJsonString = json_encode($data);
        file_put_contents($path, $newJsonString);
    }

    public function writeZeroToFile($codeType, $reDt)
    {
        $path = (new self())->pth();
        $jsonString = file_get_contents($path);
        $data = json_decode($jsonString, true);
        if ($reDt) {
            $data['expired'] = (new self())->writeUpdateDt();
        }
        $data['verify'] = $codeType;
        $newJsonString = json_encode($data);
        file_put_contents($path, $newJsonString);
    }

    public static function write()
    {
        $path = (new self())->pth();
        $jsonString = file_get_contents($path);
        $data = json_decode($jsonString, true);
        $data['expired'] = (new self())->writeUpdateDt();
        $data['verify'] = 1;
        $newJsonString = json_encode($data);
        file_put_contents($path, $newJsonString);
        echo "Success";
    }

    public static function domain()
    {
        echo request()->getHttpHost();
    }

    public function addDate()
    {
        $date = date_create(date("Y-m-d"));
        return date_add($date, date_interval_create_from_date_string("14 days"));
    }

    public function connectSrv($code)
    {
        $params = ['query' => ['code' => $code, 'url' => request()->getHttpHost()]];
        $client = new Client();
        $url = 'https://connectwithdev.com/purchase/public';
        return $client->request('GET', $url, $params);
    }

    public function writeUpdateDt()
    {
        $date = (new self())->addDate();
        return date_format($date, "Y-m-d");
    }

    public function pth()
    {
        return base_path('storage/app/helpers/helper.json');
    }

    public function pthBc()
    {
        return base_path('storage/app/helpers/tSKey.json');
    }

    public function writeBcKey($code)
    {
        $path = (new self())->pthBc();
        $jsonString = file_get_contents($path);
        $data = json_decode($jsonString, true);
        $data['key'] = $code;
        $newJsonString = json_encode($data);
        file_put_contents($path, $newJsonString);
    }

    public function gtBcKey()
    {
        $path = (new self())->pthBc();
        $jsonString = file_get_contents($path);
        $data = json_decode($jsonString, true);
        if ($data['key'] != '') {
            return $data['key'];
        }

        return '0000';
    }
}
