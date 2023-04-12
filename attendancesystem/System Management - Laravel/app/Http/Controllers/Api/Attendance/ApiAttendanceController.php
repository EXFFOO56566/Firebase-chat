<?php

namespace App\Http\Controllers\Api\Attendance;

use App\Http\Controllers\Controller;
use App\Models\Area;
use App\Models\Attendance;
use App\Models\Location;
use App\Models\Setting;
use Illuminate\Http\Request;
use Response;
use Carbon\Carbon;
use Config;

class ApiAttendanceController extends Controller
{
    /**
     * Store data attendance to DB
     * @param Request $request
     * @return \Illuminate\Http\Response
     */
    public function apiSaveAttendance(Request $request)
    {
        // Get all request
        $new = $request->all();

        // Get data setting
        $getSetting = Setting::find(1);

        // Get data from request
        $key = $new['key'];

        // Get user position
        $lat = $new['lat'];
        $longt = $new['longt'];

        $areaId = $new['area_id'];
        $q = $new['q'];
        $WorkerId = $new['worker_id'];

        $date = Carbon::now()->timezone($getSetting->timezone)->format('Y-m-d');

        if (!empty($key)) {
            if ($key == $getSetting->key_app) {

                // Check if user inside the area
                $getPoly = Location::whereIn('area_id', [$areaId])->get(['lat', 'longt']);
                if ($getPoly->count() == 0) {
                    $data = [
                        'message' => 'location not found',
                    ];
                    return response()->json($data);
                }
                $isInside = $this->isInsidePolygon($lat, $longt, $getPoly);
                if (!$isInside) {
                    $data = [
                        'message' => 'cannot attend',
                    ];
                    return response()->json($data);
                }

                // Check-in
                if ($q == 'in') {

                    // Get data from request
                    $in_time = new Carbon(Carbon::now()->timezone($getSetting->timezone)->format('H:i:s'));

                    // Check if user already check-in
                    $checkAlreadyCheckIn = Attendance::where('worker_id', $WorkerId)
                        ->where('date', Carbon::now()->timezone($getSetting->timezone)->format('Y-m-d'))
                        ->where('in_time', '<>', null)
                        ->where('late_time', '<>', null)
                        ->where('out_time', null)
                        ->where('out_location_id', null)
                        ->first();

                    if ($checkAlreadyCheckIn) {
                        $data = [
                            'message' => 'already check-in',
                        ];
                        return response()->json($data);
                    }

                    // Get late time
                    $startHour = Carbon::createFromFormat('H:i:s', $getSetting->start_time);
                    if (!$in_time->gt($startHour)) {
                        $lateTime = "00:00:00";
                    } else {
                        $lateTime = $in_time->diff($startHour)->format('%H:%I:%S');
                    }

                    $location = Area::find($areaId)->name;

                    // Save the data
                    $save = new Attendance();
                    $save->worker_id = $WorkerId;
                    $save->date = $date;
                    $save->in_location_id = $areaId;
                    $save->in_time = $in_time;
                    $save->late_time = $lateTime;

                    $createNew = $save->save();

                    // Saving
                    if ($createNew) {
                        $data = [
                            'message' => 'Success!',
                            'date' => Carbon::parse($date)->format('Y-m-d'),
                            'time' => Carbon::parse($in_time)->format('H:i:s'),
                            'location' => $location,
                            'query' => 'Check-in',
                        ];
                        return response()->json($data);
                    }

                    $data = [
                        'message' => 'Error! Something Went Wrong!',
                    ];
                    return response()->json($data);
                }

                // Check-out
                if ($q == 'out') {
                    // Get data from request
                    $out_time = new Carbon(Carbon::now()->timezone($getSetting->timezone)->format('H:i:s'));
                    $getOutHour = new Carbon($getSetting->out_time);

                    // Get data in_time from DB
                    // To get data work hour
                    $getInTime = Attendance::where('worker_id', $WorkerId)
                        ->where('date', Carbon::now()->timezone($getSetting->timezone)->format('Y-m-d'))
                        ->where('out_time', null)
                        ->where('out_location_id', null)
                        ->first();

                    if (!$getInTime) {
                        $data = [
                            'message' => 'check-in first',
                        ];
                        return response()->json($data);
                    }

                    $in_time = Carbon::createFromFormat('H:i:s', $getInTime->in_time);

                    // Get data total working hour
                    $getWorkHour = $out_time->diff($in_time)->format('%H:%I:%S');

                    // Get over time
                    if ($in_time->gt($getOutHour) || !$out_time->gt($getOutHour)) {
                        $getOverTime = "00:00:00";
                    } else {
                        $getOverTime = $out_time->diff($getOutHour)->format('%H:%I:%S');
                    }

                    // Early out time
                    if ($in_time->gt($getOutHour)) {
                        $earlyOutTime = "00:00:00";
                    } else {
                        $earlyOutTime = $getOutHour->diff($out_time)->format('%H:%I:%S');
                    }

                    $location = Area::find($areaId)->name;

                    // Update the data
                    $getInTime->out_time = $out_time;
                    $getInTime->over_time = $getOverTime;
                    $getInTime->work_hour = $getWorkHour;
                    $getInTime->early_out_time = $earlyOutTime;
                    $getInTime->out_location_id = $areaId;

                    $updateData = $getInTime->save();

                    // Updating
                    if ($updateData) {
                        $data = [
                            'message' => 'Success!',
                            'date' => Carbon::parse($date)->format('Y-m-d'),
                            'time' => Carbon::parse($out_time)->format('H:i:s'),
                            'location' => $location,
                            'query' => 'Check-Out',
                        ];
                        return response()->json($data);
                    }
                    $data = [
                        'message' => 'Error! Something Went Wrong!',
                    ];
                    return response()->json($data);
                }
                $data = [
                    'message' => 'Error! Wrong Command!',
                ];
                return response()->json($data);
            }
            $data = [
                'message' => 'The KEY is Wrong!',
            ];
            return response()->json($data);
        }
        $data = [
            'message' => 'Please Setting KEY First!',
        ];
        return response()->json($data);
    }

    /**
     * Check if user inside the area
     * @param $x
     * @param $y
     * @param $polygon
     * @return \Illuminate\Http\Response
     */
    public function isInsidePolygon($x, $y, $polygon)
    {
        $inside = false;
        for ($i = 0, $j = count($polygon) - 1, $iMax = count($polygon); $i < $iMax; $j = $i++) {
            $xi = $polygon[$i]['lat'];
            $yi = $polygon[$i]['longt'];
            $xj = $polygon[$j]['lat'];
            $yj = $polygon[$j]['longt'];

            $intersect = (($yi > $y) != ($yj > $y))
                && ($x < ($xj - $xi) * ($y - $yi) / ($yj - $yi) + $xi);
            if ($intersect) {
                $inside = !$inside;
            }
        }

        return $inside;
    }
}
