<?php

namespace App\Http\Controllers\Utils\Activity;

use App\Http\Controllers\Controller;
use Auth;

class SaveActivityLogController extends Controller
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
     * Show the application dashboard.
     *
     * @param array $new
     * @param String $log
     * @return void
     */
    public function saveLog(array $new, String $log)
    {
        // Record activity
        activity()
            ->causedBy(Auth::user()->id)
            ->withProperties($new)
            ->log($log);
        // Record activity
    }
}
