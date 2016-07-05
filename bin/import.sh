#!/bin/bash

RED='\e[1m\e[31m'
GREEN='\e[1m\e[92m'
NORMAL='\e[0m'
set -e

while [[ $# -gt 0 ]]
do
key="$1"

case ${key} in
    -f|--file)
    FILE="$2"
    shift
    ;;
    -c|--clear-database)
    CLEAR_DB=true
    ;;
    -h|--help)
    echo -e "-f --file path/to/file\t\tFile which contains OSM Nodes\n-c --clear-database \t\tIf set, records from table point_of_interest will be deleted before inserting new records."
    exit 0
    ;;
    *)
    echo "Unknown parameter $1."
    exit 1
    ;;
esac
shift
done

CSV_FILE="tmp/filtered.csv"

if ! [ -f "$FILE" ]; then
    echo -e "${RED}Please provide a osm file, e.g. ./import.sh -f nodes.osm"
    exit 1
fi

if [ -d "tmp/" ]; then
	echo -n -e "${NORMAL}Cleaning old tmp files...\t\t"
	rm -rf tmp/
	if [ $? -eq 0 ]; then
	    echo -e ${GREEN}OK
	else
	    echo -e ${RED}FAIL
	fi
fi

echo -n -e "${NORMAL}Creating tmp directory...\t\t"
mkdir tmp
if [ $? -eq 0 ]; then
    echo -e ${GREEN}OK
else
    echo -e ${RED}FAIL
fi

echo -n -e "${NORMAL}Filter OSM nodes...\t\t\t"
bin/osmconvert64 ${FILE} --csv="@id @lat @lon @version @changeset access addr_housename addr_housenumber addr_interpolation admin_level aerialway aeroway amenity area barrier bicycle brand bridge boundary building capital construction covered culvert cutting denomination disused ele embankment foot generator_source harbour highway historic horse intermittent junction landuse layer leisure ship_lock man_made military motorcar name osm_natural office oneway operator place poi population power power_source public_transport railway ref religion route service shop sport surface toll tourism tower_type tunnel water waterway wetland width wikipedia wood" -o=${CSV_FILE}  --csv-separator=,
if [ $? -eq 0 ]; then
    echo -e ${GREEN}OK
else
    echo -e ${RED}FAIL
fi

echo -n -e "${NORMAL}Create Database trip_planner...\t\t"
sudo mysql -e "CREATE DATABASE IF NOT EXISTS trip_planner;"
if [ $? -eq 0 ]; then
    echo -e ${GREEN}OK
else
    echo -e ${RED}FAIL
fi

FILTERED_CNT=$(awk '{n+=1} END {print n}' ${CSV_FILE})

if [ "${CLEAR_DB}" = true ] ; then
    echo -n -e "${NORMAL}Drop table trip_planner...\t\t"
    sudo mysql -e "DROP TABLE IF EXISTS point_of_interest" trip_planner
    if [ $? -eq 0 ]; then
        echo -e ${GREEN}OK
    else
        echo -e ${RED}FAIL
    fi
fi

echo -n -e "${NORMAL}Creating table...\t\t\t"
sudo mysql -e "CREATE TABLE IF NOT EXISTS point_of_interest ( poi_id BIGINT unsigned NOT NULL auto_increment, osm_id BIGINT, PRIMARY KEY  (poi_id), point POINT NOT NULL, SPATIAL INDEX(point),  version TEXT, changeset TEXT, access TEXT, addr_housename TEXT, addr_housenumber TEXT, addr_interpolation TEXT, admin_level TEXT, aerialway TEXT, aeroway TEXT, amenity TEXT, area TEXT, barrier TEXT, bicycle TEXT, brand TEXT, bridge TEXT, boundary TEXT, building TEXT, capital TEXT, construction TEXT, covered TEXT, culvert TEXT, cutting TEXT, denomination TEXT, disused TEXT, ele TEXT, embankment TEXT, foot TEXT, generator_source TEXT, harbour TEXT, highway TEXT, historic TEXT, horse TEXT, intermittent TEXT, junction TEXT, landuse TEXT, layer TEXT, leisure TEXT, ship_lock TEXT, man_made TEXT, military TEXT, motorcar TEXT, name TEXT, osm_natural TEXT, office TEXT, oneway TEXT, operator TEXT, place TEXT, poi TEXT, population TEXT, power TEXT, power_source TEXT, public_transport TEXT, railway TEXT, ref TEXT, religion TEXT, route TEXT, service TEXT, shop TEXT, sport TEXT, surface TEXT, toll TEXT, tourism TEXT, tower_type TEXT, tunnel TEXT, water TEXT, waterway TEXT, wetland TEXT, width TEXT, wikipedia TEXT) ENGINE=MyISAM;" trip_planner
if [ $? -eq 0 ]; then
    echo -e ${GREEN}OK
else
    echo -e ${RED}FAIL
fi

cp ${CSV_FILE} tmp/inserts.sql
echo -n -e "${NORMAL}Creating SQL statements...\t\t"
# replace empty CSV value with NULL
sed 's/^,/NULL,/; :a;s/,,/,NULL,/g;ta' -i tmp/inserts.sql
# remove last , because wood is always empty
sed 's/a$/,/' -i tmp/inserts.sql
#replace " with '
sed -e ':a' -e 'N' -e '$!ba' -e 's/\"/\x27/g' -i tmp/inserts.sql
# enquote every value
sed 's/[^,][^,]*/"&"/g' -i tmp/inserts.sql
# replace ,, with ,NULL,
sed 's/,,/,NULL,NULL,/g' -i tmp/inserts.sql
# replace ,, with ,
sed 's/,,/,/g' -i tmp/inserts.sql
# add INSERT INTO point_of_interest VALUES (NULL, before each line
sed 's/^/INSERT INTO point_of_interest VALUES (NULL,/' -i tmp/inserts.sql
# add ); at the end of each line
sed 's/$/);/' -i tmp/inserts.sql
# replace ,); with );
sed 's/,);/);/g' -i tmp/inserts.sql

# Create Point(lat lon) from "lat", "lon"
sed 's/,"/,ST_PointFromText("POINT(/2' -i tmp/inserts.sql
sed 's/,/ /3' -i tmp/inserts.sql
sed 's/",/)"),/2' -i tmp/inserts.sql
sed 's/"//4' -i tmp/inserts.sql
sed 's/"//4' -i tmp/inserts.sql
if [ $? -eq 0 ]; then
    echo -e ${GREEN}OK
else
    echo -e ${RED}FAIL
fi

echo -n -e "${NORMAL}Executing SQL statements...\t\t"
sudo mysql trip_planner < tmp/inserts.sql 
if [ $? -eq 0 ]; then
    echo -e ${GREEN}OK
else
    echo -e ${RED}FAIL
fi

ROW_CNT=$(sudo mysql trip_planner --raw --batch -e 'select count(*) from point_of_interest' -s)
echo -e "${NORMAL}Imported records: \t\t\t$ROW_CNT"

if ! [ ${ROW_CNT} == ${FILTERED_CNT} ]; then
    echo -e "${RED}WARNING: Imported $FILTERED_CNT records, but MySQL inserted $ROW_CNT records!";
fi

echo -n -e "${NORMAL}Cleaning tmp files...\t\t\t"
rm -r tmp/
if [ $? -eq 0 ]; then
    echo -e ${GREEN}OK
else
    echo -e ${RED}FAIL
fi
echo -e "${GREEN}Finished successfully."
exit 0
