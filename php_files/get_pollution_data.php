<?php
define('HOST','EDIT_HERE');
define('USER','EDIT_HERE');
define('PASS','EDIT_HERE');
define('DB','EDIT_HERE');
$con = mysqli_connect(HOST,USER,PASS,DB);
$sql = "select * from pollution_values";
$res = mysqli_query($con,$sql);
$result = array();
while($row = mysqli_fetch_array($res)){
array_push($result,
array('gas1_value'=>$row[0],
'gas2_value'=>$row[1],
'gas3_value'=>$row[2],
'time_value'=>$row[3],
'location_lat_value'=>$row[4],
'location_lng_value'=>$row[5]
));
}
echo json_encode(array("result"=>$result));
mysqli_close($con);
?>