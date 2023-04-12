<?php

namespace App\Http\Controllers;

use Illuminate\Support\Facades\Storage;

class MainController extends Controller
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

    public function checkProductVerify()
    {
        $exists = Storage::disk('local')->exists('helpers/helper.json');
        $chk = 0;
        if ($exists) {
            $path = Storage::disk('local')->get('helpers/helper.json');
            $content = json_decode($path, true);
            $chk = $content['verify'];
        }

        return $chk;
    }
}
