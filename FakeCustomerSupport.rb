require 'aws-sdk-dynamodb'  # v2: require 'aws-sdk'
require 'faker'

# Create dynamodb client in us-west-2 region
dynamodb = Aws::DynamoDB::Client.new(region: 'eu-west-2', 
access_key_id: "",
secret_access_key: ""
  )


start = 1


def makedoc(start, i, dynamodb)

  custEmail = Faker::Internet.free_email
  table = "CustomerSupport"

  email = {
      :'customerID' => "CUST"+start.to_s+(50+i).to_s,
      :'from' => Faker::Internet.free_email,
      :'type' => "email",
      :'to' => "CustomerSupport@bullworkbank.com",
      #:'received' => Time.now.to_s,
      :'received' => i.to_s,
      :'subject' => Faker::HitchhikersGuideToTheGalaxy.quote,
      :'body' => Faker::Lorem.paragraphs(1),
    }
    
    phone = {
      :'customerID' => "CUST"+start.to_s+(250+i).to_s,
      :'from' => Faker::PhoneNumber.phone_number,
      :'type' => "phone",
      :'to' => "CustomerSupport line" + (i/100).to_int.to_s,
      #:'received' => Time.now.to_s,
      :'received' => i.to_s,
      :'lengthSeconds' => rand(250),
      :'recordingURI' => "http://recording.org/"+(rand(100000)+1000000).to_s+"/rec.mp3",
    }
    
    chat = {
      :'type' => "chat",
      :'customerID' => "CUST"+start.to_s+(250+i).to_s,
      :'from' => custEmail,
      :'to' => "Agent"+(20+i).to_s,
      #:'received' => Time.now.to_s,
      :'received' => i.to_s,
      :'chat' => {
        :'from' => custEmail,
        :'ts' => Time.now.to_s,
        :'content' => Faker::HitchhikersGuideToTheGalaxy.quote,
        :'reply' => "Agent"+(20+i).to_s,
        :'ts2' => Time.now.to_s,
        :'content2' => Faker::HitchhikersGuideToTheGalaxy.quote,
      }
    }
    
    
      messageType = rand(3)
    
      case messageType
      when 0
        params = {
          table_name: table,
          item: email
        }
      when 1
        params = {
          table_name: table,
          item: phone
      }
      when 2
        params = {
          table_name: table,
          item: chat
      }
      else
        "Error: capacity has an invalid value (#{messageType})"
      end 

  begin
    result = dynamodb.put_item(params)
    puts 'Added thing: ' + params.to_s 
  rescue  Aws::DynamoDB::Errors::ServiceError => error
    puts 'Unable to add movie:'
    puts error.message
  end
       
end


for i in 1..1000000
  makedoc(start, i, dynamodb)
end
